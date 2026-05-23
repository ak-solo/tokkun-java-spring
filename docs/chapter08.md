# ch08 新画面作成（CRUD 完成）

## このチャプターで学ぶこと

- ch07 で作ったプロジェクト画面に**登録・編集・削除**を追加する
- バリデーション条件を仕様書から読み取って自分で実装する
- 複数テーブルにまたがる INSERT / DELETE（`employee_projects` の操作）を実装する
- 画面遷移の設計（どの操作後にどこへリダイレクトするか）を自分で考えて実装する

---

## 前提

- スターターコードは提供しない
- ch03〜ch05 で実装した `employees` の CRUD を参考にしながら実装すること
- 詰まったときは [ヒント](#ヒント) を参照すること

---

## 作成する画面の仕様

### 画面 3：プロジェクト新規登録（`/projects/new`）

#### URL

| メソッド | パス | 説明 |
|---------|------|------|
| GET | `/projects/new` | 登録フォーム表示 |
| POST | `/projects` | 登録実行 |

#### フォーム項目

| 項目 | 入力形式 | 必須 | バリデーション |
|------|---------|------|----------------|
| プロジェクト名 | テキスト | 必須 | 空白不可（`@NotBlank`） |
| 開始日 | 日付（`type="date"`） | 必須 | 空白不可（`@NotNull`） |
| 終了日 | 日付（`type="date"`） | 任意 | 省略可能（`null` 許容） |
| 予算 | 数値 | 必須 | 1 以上（`@Min(1)`） |

#### 登録後のリダイレクト先

登録成功後は、登録したプロジェクトの詳細画面（`/projects/{id}`）にリダイレクトすること。

---

### 画面 4：プロジェクト編集（`/projects/{id}/edit`）

#### URL

| メソッド | パス | 説明 |
|---------|------|------|
| GET | `/projects/{id}/edit` | 編集フォーム表示（既存データを初期値として表示） |
| POST | `/projects/{id}/edit` | 編集実行 |

#### フォーム項目

登録フォームと同じ項目・同じバリデーション条件。

#### 編集後のリダイレクト先

編集成功後は、そのプロジェクトの詳細画面（`/projects/{id}`）にリダイレクトすること。

---

### 画面 5：プロジェクト削除確認（`/projects/{id}/delete`）

#### URL

| メソッド | パス | 説明 |
|---------|------|------|
| GET | `/projects/{id}/delete` | 削除確認画面表示 |
| POST | `/projects/{id}/delete` | 削除実行 |

#### 表示内容

削除対象のプロジェクト情報（ID・プロジェクト名・予算）を表示し、「削除する」ボタンと「キャンセル」リンクを表示すること。

#### 削除時の注意

プロジェクトを削除する前に、`employee_projects` テーブルの関連レコードを先に削除すること（外部キー制約があるため）。

```
1. DELETE FROM employee_projects WHERE project_id = :id
2. DELETE FROM projects WHERE id = :id
```

#### 削除後のリダイレクト先

削除成功後はプロジェクト一覧画面（`/projects`）にリダイレクトすること。

---

## バリデーション条件まとめ

| フィールド | アノテーション | エラーメッセージ例 |
|-----------|----------------|-------------------|
| `name` | `@NotBlank` | 「プロジェクト名は入力必須です」 |
| `startDate` | `@NotNull` | 「開始日は入力必須です」 |
| `budget` | `@Min(1)` | 「予算は 1 以上を入力してください」 |

バリデーションエラー時はフォーム画面に戻り、`th:errors` でエラーメッセージを表示すること。

---

## 確認ポイント

- 「新規登録」ボタンから登録フォームに遷移し、登録後に詳細画面へ移動すること
- プロジェクト名を空にして登録しようとするとエラーメッセージが表示されること
- 予算に 0 以下の値を入力するとエラーメッセージが表示されること
- 終了日を空欄のまま登録でき、詳細画面で「進行中」と表示されること
- 詳細画面から編集フォームに遷移し、既存データが入力欄に表示されていること
- 削除確認画面で「削除する」を押すとプロジェクトが削除され一覧に戻ること
- 削除後、`employee_projects` のデータも合わせて削除されていること

---

## ヒント

詰まったときだけ読むこと。

### Model に validation アノテーションを追加する

`Employee.java` を参考に、`Project.java` のフィールドに Bean Validation アノテーションを追加する。

```java
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

public class Project {
    private int id;

    @NotBlank(message = "プロジェクト名は入力必須です")
    private String name;

    @NotNull(message = "開始日は入力必須です")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;   // null 許容

    @Min(value = 1, message = "予算は 1 以上を入力してください")
    private Integer budget;

    // getter / setter
}
```

### Repository に save / update / delete を追加する

`EmployeeRepository.java` の `save`・`update`・`delete` を参考に実装する。

**save（INSERT）**

```java
public int save(Project project) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("name",      project.getName())
        .addValue("startDate", project.getStartDate())
        .addValue("endDate",   project.getEndDate())   // null でも OK
        .addValue("budget",    project.getBudget());
    jdbcTemplate.update(
        "INSERT INTO projects (name, start_date, end_date, budget)" +
        " VALUES (:name, :startDate, :endDate, :budget)",
        params, keyHolder, new String[]{"id"}
    );
    return keyHolder.getKey().intValue();
}
```

**delete（2ステップ）**

```java
public void delete(int id) {
    jdbcTemplate.update(
        "DELETE FROM employee_projects WHERE project_id = :id",
        Map.of("id", id)
    );
    jdbcTemplate.update(
        "DELETE FROM projects WHERE id = :id",
        Map.of("id", id)
    );
}
```

### Controller にアクションを追加する

`EmployeeController.java` の `createForm`・`create`・`editForm`・`edit`・`deleteConfirm`・`delete` を参考に実装する。

`edit` アクション（POST）は `@Valid` と `BindingResult` を受け取り、エラー時はフォームに戻る：

```java
@PostMapping("/{id}/edit")
public String edit(
        @PathVariable int id,
        @Valid @ModelAttribute Project project,
        BindingResult result) {
    if (result.hasErrors()) {
        return "project/edit";
    }
    projectRepository.update(project);
    return "redirect:/projects/" + id;
}
```

### View を作る

`templates/employee/create.html`・`edit.html`・`delete.html` を参考に作成する。

編集フォームには `th:object`・`th:field`・`th:errors` を使う（ch04 で学んだパターン）。

終了日は任意入力のため、バリデーションエラーの表示は不要。
