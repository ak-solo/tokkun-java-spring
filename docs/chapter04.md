# ch04 編集

## このチャプターで学ぶこと

- Bean Validation でフォームの入力値を検証する方法（`@NotBlank`・`@Min`・`@Max`）
- `@Valid` と `BindingResult` でコントローラーがエラーを受け取る方法
- Thymeleaf の `th:object`・`th:field`・`th:errors` でフォーム連携とエラー表示をする方法
- JdbcTemplate で UPDATE を実行する方法

---

## 基礎知識

### Bean Validation とは

Bean Validation は、Java オブジェクトのフィールドに **アノテーション** を付けるだけで入力チェックができる仕組みです。

```java
// model/Employee.java
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class Employee {

    @NotBlank(message = "氏名は入力必須です")
    private String name;

    @Min(value = 1, message = "給与は 1 以上を入力してください")
    private Integer salary;
}
```

| アノテーション | 対象 | 内容 |
|---|---|---|
| `@NotBlank` | String | `null`・空文字・空白だけの文字列を拒否する |
| `@NotNull` | 任意 | `null` を拒否する |
| `@Min(value)` | 数値 | 指定値未満を拒否する |
| `@Max(value)` | 数値 | 指定値超過を拒否する |
| `@Size(min, max)` | String | 文字数の範囲を指定する |
| `@PastOrPresent` | 日付 | 未来の日付を拒否する |

### @Valid と BindingResult

コントローラーの POST メソッドに `@Valid` を付けると、バリデーションが実行されます。  
`BindingResult` を引数に加えると、エラーがあるかどうかを確認できます。

```java
@PostMapping("/{id}/edit")
public String edit(
        @PathVariable int id,
        @Valid @ModelAttribute Employee employee,
        BindingResult result) {

    // バリデーションエラーがあれば編集フォームを再表示
    if (result.hasErrors()) {
        return "employee/edit";
    }

    // エラーがなければ UPDATE して詳細画面へ
    employeeRepository.update(employee);
    return "redirect:/employees/" + id;
}
```

> **重要**: `BindingResult` は必ず `@Valid` を付けた引数の **直後** に書くこと。  
> 順番が逆だとエラーが正しく捕捉されません。

### th:object・th:field・th:errors

バリデーションエラーを表示するには、フォームを `th:object`・`th:field` で書き直す必要があります。

#### th:object ─ フォームとモデルオブジェクトを紐づける

```html
<form th:action="..." method="post" th:object="${employee}">
```

`th:object="${employee}"` で、このフォームが `employee` オブジェクトと対応することを示します。

#### th:field ─ フィールド名・初期値・HTML の name 属性を一括設定する

```html
<!-- th:field を使う前（ch03 スターター） -->
<input type="text" name="name" th:value="${employee.name}" class="form-control" />

<!-- th:field を使った後（ch04） -->
<input type="text" th:field="*{name}" class="form-control" />
```

`th:field="*{name}"` は、フォームの `th:object` オブジェクトの `name` フィールドを参照します。  
`*{...}` は `th:object="${employee}"` のオブジェクトへのショートカットです。  
`id="name"`・`name="name"`・`value="田中 太郎"` の 3 つを一括で生成します。

#### th:errors ─ バリデーションエラーを表示する

```html
<div class="mb-3">
    <label class="form-label">氏名</label>
    <input type="text" th:field="*{name}" class="form-control" />
    <span th:errors="*{name}" class="text-danger small"></span>
</div>
```

`th:errors="*{name}"` はバリデーションエラーがあるときだけ、エラーメッセージを表示します。  
エラーがなければ `<span>` 要素は表示されません。

### JdbcTemplate での UPDATE

tokkun-sql で学んだ UPDATE 文をそのまま使います。

```sql
-- tokkun-sql で書いた UPDATE
UPDATE employees
SET name = '佐藤 三郎', salary = 65000
WHERE id = 3;
```

Java での実装：

```java
public void update(Employee employee) {
    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("name",     employee.getName())
        .addValue("salary",   employee.getSalary())
        .addValue("hireDate", employee.getHireDate())
        .addValue("id",       employee.getId());
    jdbcTemplate.update(
        "UPDATE employees SET name = :name, salary = :salary, hire_date = :hireDate WHERE id = :id",
        params
    );
}
```

---

## スターターコードの確認

現在の状態を確認しましょう。

**動いていること**
- `GET /employees/{id}/edit` で社員情報が入力済みのフォームが表示される
- 「キャンセル」で詳細画面に戻れる

**まだ動いていないこと**
- 「更新」ボタンを押すと 405 エラーになる（POST ハンドラがない）
- バリデーションがない（空欄で送信してもエラーにならない）
- 削除機能がない

確認すべきファイル：

```
controller/EmployeeController.java   ← editForm(GET) はあるが edit(POST) がない
model/Employee.java                  ← @NotBlank 等がついていない
templates/employee/edit.html         ← th:value 形式・th:errors なし
```

---

## 練習問題

### 問題 04-1：edit.html のフォームを th:object / th:field に書き換える

**目標**: バリデーションエラーを表示できるよう、`edit.html` のフォームを `th:object`・`th:field` 形式に書き換える。

**編集するファイル**
- `templates/employee/edit.html`

**手順のヒント**

```html
<!-- 変更前 -->
<form th:action="@{/employees/{id}/edit(id=${employee.id})}" method="post">
    <input type="hidden" name="id" th:value="${employee.id}" />
    <input type="text" name="name" th:value="${employee.name}" class="form-control" />

<!-- 変更後 -->
<form th:action="@{/employees/{id}/edit(id=${employee.id})}" method="post"
      th:object="${employee}">
    <input type="hidden" th:field="*{id}" />
    <input type="text" th:field="*{name}" class="form-control" />
```

変更のポイント：
- `<form>` タグに `th:object="${employee}"` を追加する
- `<input type="hidden" name="id" th:value="...">` → `<input type="hidden" th:field="*{id}">`
- 各フィールドの `name="..."` と `th:value="..."` を `th:field="*{...}"` にまとめる

> **ポイント**: `th:field="*{name}"` の `*{...}` は、`th:object` で指定したオブジェクト（`employee`）への参照です。

**確認ポイント**
- `GET /employees/1/edit` で田中 太郎の情報が表示されること（動作は変わらない）
- ブラウザのソースを確認して `name="name"`・`value="田中 太郎"` が出力されていること

---

### 問題 04-2：Employee.java にバリデーションを追加する

**目標**: 氏名が空欄のとき・給与が不正な値のときにエラーを出す。

**編集するファイル**
- `model/Employee.java`

**手順のヒント**

ファイルの先頭の `import` に追加する：

```java
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
```

フィールドにアノテーションを追加する：

```java
@NotBlank(message = "氏名は入力必須です")
private String name;

@Min(value = 1, message = "給与は 1 以上を入力してください")
@Max(value = 99999999, message = "給与は 99,999,999 以下を入力してください")
private Integer salary;
```

**確認ポイント**
- ビルドエラーが出ないこと（`./gradlew build` で確認）
- （まだ POST ハンドラーがないので、画面での動作は変わりません）

---

### 問題 04-3：edit.html にエラーメッセージ表示を追加する

**目標**: バリデーションエラーがあるとき、各フィールドの下にエラーメッセージを表示する。

**編集するファイル**
- `templates/employee/edit.html`

**手順のヒント**

各フィールドの `<input>` の下に `<span th:errors="...">` を追加する。

```html
<div class="mb-3">
    <label class="form-label">氏名</label>
    <input type="text" th:field="*{name}" class="form-control" />
    <span th:errors="*{name}" class="text-danger small"></span>
</div>
```

給与フィールドにも同様に追加する：

```html
<div class="mb-3">
    <label class="form-label">給与</label>
    <input type="number" th:field="*{salary}" class="form-control" />
    <span th:errors="*{salary}" class="text-danger small"></span>
</div>
```

**確認ポイント**
- ビルドエラーが出ないこと
- （まだ POST ハンドラーがないので、エラーメッセージが実際に表示される確認は次の問題で行います）

---

### 問題 04-4：Repository に UPDATE メソッドを実装する

**目標**: `EmployeeRepository` に `update` メソッドを追加し、編集した内容を DB に反映する。

**編集するファイル**
- `repository/EmployeeRepository.java`

**手順のヒント**

```java
public void update(Employee employee) {
    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("name",     employee.getName())
        .addValue("deptId",   employee.getDeptId())
        .addValue("salary",   employee.getSalary())
        .addValue("hireDate", employee.getHireDate())
        .addValue("id",       employee.getId());
    jdbcTemplate.update(
        "UPDATE employees" +
        " SET name = :name, dept_id = :deptId, salary = :salary, hire_date = :hireDate" +
        " WHERE id = :id",
        params
    );
}
```

> **ポイント**: `WHERE id = :id` の条件を忘れると全件が更新されてしまいます。  
> tokkun-sql の ch08 で学んだ「WHERE なし UPDATE の危険性」と同じです。

**確認ポイント**
- ビルドエラーが出ないこと
- （まだコントローラーから呼んでいないので画面の動作は変わりません）

---

### 問題 04-5：edit（POST）アクションを実装する

**目標**: `POST /employees/{id}/edit` を受け取り、バリデーション → UPDATE → 詳細画面へリダイレクトする処理を実装する。

**編集するファイル**
- `controller/EmployeeController.java`

**手順のヒント**

`import` に追加する：

```java
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
```

`editForm` の後に `edit` メソッドを追加する：

```java
@PostMapping("/{id}/edit")
public String edit(
        @PathVariable int id,
        @Valid @ModelAttribute Employee employee,
        BindingResult result) {
    if (result.hasErrors()) {
        return "employee/edit";
    }
    employeeRepository.update(employee);
    return "redirect:/employees/" + id;
}
```

**確認ポイント**
- 編集フォームで正しい値を入力して「更新」を押すと詳細画面に変更が反映されること
- 氏名を空にして「更新」を押すと「氏名は入力必須です」のエラーが表示されること
- 給与に `0` や負の数を入力すると「給与は 1 以上を入力してください」が表示されること
- エラー時にフォームの入力値が保持されること（入力しなおしが不要）

---

### 問題 04-6：削除確認画面の土台を作る

**目標**: `GET /employees/3/delete` にアクセスすると削除対象の社員情報と確認ボタンが表示されるようにする。  
実際の DELETE 実行は次章で実装します。

**編集・追加するファイル**
- `controller/EmployeeController.java`（`deleteConfirm` メソッドを追加）
- `templates/employee/delete.html`（新規作成）
- `templates/employee/detail.html`（「削除」リンクを追加）
- `templates/employee/index.html`（「削除」リンクを追加）

#### ステップ 1：Controller に deleteConfirm を追加する

```java
@GetMapping("/{id}/delete")
public String deleteConfirm(@PathVariable int id, Model model) {
    Employee employee = employeeRepository.findById(id);
    model.addAttribute("employee", employee);
    return "employee/delete";
}
```

#### ステップ 2：delete.html を新規作成する

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/default}">
<head>
    <title>社員削除</title>
</head>
<body>
<div layout:fragment="content">
    <h1>社員削除</h1>

    <p>以下の社員を削除してよいですか？</p>

    <dl class="row">
        <dt class="col-sm-2">ID</dt>
        <dd class="col-sm-10" th:text="${employee.id}"></dd>

        <dt class="col-sm-2">氏名</dt>
        <dd class="col-sm-10" th:text="${employee.name}"></dd>
    </dl>

    <form th:action="@{/employees/{id}/delete(id=${employee.id})}" method="post">
        <button type="submit" class="btn btn-danger">削除する</button>
        <a th:href="@{/employees/{id}(id=${employee.id})}" class="btn btn-secondary">キャンセル</a>
    </form>
</div>
</body>
</html>
```

> **なぜ POST を使うの？**: 削除は「データを変更する操作」です。  
> GET でアクセスするだけで削除が実行されると、ブラウザのプリフェッチや誤操作で意図しない削除が起きます。  
> DELETE（変更）は必ず POST（または DELETE）リクエストで実行します。

#### ステップ 3：detail.html に「削除」リンクを追加する

```html
<a th:href="@{/employees/{id}/delete(id=${employee.id})}" class="btn btn-danger">削除</a>
```

#### ステップ 4：index.html の一覧に「削除」リンクを追加する

```html
<td>
    <a th:href="@{/employees/{id}(id=${employee.id})}">詳細</a>
    <a th:href="@{/employees/{id}/edit(id=${employee.id})}">編集</a>
    <a th:href="@{/employees/{id}/delete(id=${employee.id})}" class="text-danger">削除</a>
</td>
```

**確認ポイント**
- `/employees/1/delete` で田中 太郎の確認画面が表示されること
- 「削除する」ボタンを押すと 405 エラーになること（POST ハンドラーは次章で実装）
- 「キャンセル」で詳細画面に戻れること
