# ch05 削除

## このチャプターで学ぶこと

- JdbcTemplate で DELETE を実行する方法
- GET で削除してはいけない理由
- POST による削除フローと PRG パターン
- `@RequestParam` でクエリパラメータを受け取る方法（次章の準備）

---

## 基礎知識

### DELETE の実装

tokkun-sql で学んだ DELETE 文をそのまま使います。

```sql
-- tokkun-sql で書いた DELETE
DELETE FROM employees WHERE id = 3;
```

Java での実装：

```java
// repository に追加
public void delete(int id) {
    jdbcTemplate.update(
        "DELETE FROM employees WHERE id = :id",
        Map.of("id", id)
    );
}
```

UPDATE と同じ `jdbcTemplate.update()` を使います。  
`WHERE` を忘れると全件削除になるので注意してください。

### なぜ GET で削除してはいけないのか

```
❌ 間違った設計：
<a href="/employees/3/delete">削除</a>  ← GET リクエスト

問題：
・ブラウザの「戻る」や履歴をクリックしただけで削除が実行される
・検索エンジンのクローラーが URL を辿るだけで削除される（スパイダー問題）
・ページのプリフェッチ（先読み）で意図せず削除される
・CSRF 攻撃（悪意あるサイトのリンクをクリックするだけで削除される）
```

**正しい設計**: GET で確認画面を表示し、POST でデータを変更する。

```
✅ 正しい設計：
① GET /employees/3/delete  → 確認画面を表示
② POST /employees/3/delete → 削除を実行 → リダイレクト
```

### 削除の PRG パターン

```
ユーザーが「削除する」ボタンを押す（POST）
        ↓
サーバーが DELETE を実行
        ↓
302 リダイレクト → GET /employees（一覧）
        ↓
一覧画面が表示される
```

ページを更新（F5）しても DELETE は再実行されず、GET /employees が再実行されるだけです。

### @RequestParam ─ クエリパラメータを受け取る

`GET /employees?keyword=田中` のように URL に付く `?以降の値` を**クエリパラメータ**と言います。  
`@RequestParam` でこの値を引数に受け取れます。

```java
@GetMapping
public String index(@RequestParam(defaultValue = "") String keyword, Model model) {
    // /employees にアクセス → keyword = ""（空文字）
    // /employees?keyword=田中 にアクセス → keyword = "田中"
}
```

`defaultValue = ""` は「クエリパラメータが指定されなかったときのデフォルト値」です。  
これがないと `/employees` にアクセスしたとき `keyword` が `null` になり、後続の処理でエラーが出る場合があります。

> **次章 ch06 で使います**: この章では `keyword` を受け取るだけで、実際の絞り込みは ch06 で実装します。

---

## スターターコードの確認

現在の状態を確認しましょう。

**動いていること**
- `GET /employees/{id}/delete` で削除確認画面が表示される
- 「キャンセル」で詳細画面に戻れる

**まだ動いていないこと**
- 「削除する」ボタンを押すと 405 エラーになる（POST ハンドラーがない）
- 検索フォームがない

確認すべきファイル：

```
controller/EmployeeController.java   ← deleteConfirm(GET) はあるが delete(POST) がない
repository/EmployeeRepository.java  ← delete メソッドがない
templates/employee/delete.html      ← ID と氏名しか表示されていない
```

---

## 練習問題

### 問題 05-1：削除確認画面に「部署名」「給与」「入社日」を追加する

**目標**: 削除確認画面に部署名・給与・入社日を追加し、削除対象を正確に確認できるようにする。

**編集するファイル**
- `templates/employee/delete.html`

**手順のヒント**

`deleteConfirm` アクションは `findById` を使ってデータを取得しています。  
`findById` は `departments` テーブルと LEFT JOIN しているので、`employee.deptName` も利用できます。

```html
<dl class="row">
    <dt class="col-sm-2">ID</dt>
    <dd class="col-sm-10" th:text="${employee.id}"></dd>

    <dt class="col-sm-2">氏名</dt>
    <dd class="col-sm-10" th:text="${employee.name}"></dd>

    <!-- 以下を追加 -->
    <dt class="col-sm-2">部署名</dt>
    <dd class="col-sm-10" th:text="${employee.deptName} ?: '（未所属）'"></dd>

    <dt class="col-sm-2">給与</dt>
    <dd class="col-sm-10" th:text="${employee.salary}"></dd>

    <dt class="col-sm-2">入社日</dt>
    <dd class="col-sm-10" th:text="${employee.hireDate}"></dd>
</dl>
```

**確認ポイント**
- `/employees/1/delete` にアクセスすると田中 太郎の部署名・給与・入社日が表示されること

---

### 問題 05-2：Repository に DELETE メソッドを実装する

**目標**: `EmployeeRepository` に `delete` メソッドを追加し、指定した ID の社員を削除する。

**編集するファイル**
- `repository/EmployeeRepository.java`

**手順のヒント**

```java
public void delete(int id) {
    jdbcTemplate.update(
        "DELETE FROM employees WHERE id = :id",
        Map.of("id", id)
    );
}
```

> **ポイント**: `Map.of("id", id)` は `int` 型の `id` を受け取れます。  
> `Map.of()` は null を含む値を受け付けないので注意してください（`id` は常に非 null なので問題ありません）。

**確認ポイント**
- ビルドエラーが出ないこと（`./gradlew build` で確認）

---

### 問題 05-3：delete（POST）アクションを実装する

**目標**: `POST /employees/{id}/delete` を受け取り、DELETE を実行して一覧画面へリダイレクトする。

**編集するファイル**
- `controller/EmployeeController.java`

**手順のヒント**

`deleteConfirm` の後に `delete` メソッドを追加する。

```java
@PostMapping("/{id}/delete")
public String delete(@PathVariable int id) {
    employeeRepository.delete(id);
    return "redirect:/employees";
}
```

**確認ポイント**
- 削除確認画面で「削除する」を押すと一覧に戻ること
- 一覧から削除した社員が消えていること
- ブラウザを更新（F5）しても再削除されないこと（PRG パターンの確認）
- 削除後にブラウザの「戻る」ボタンを押すとどうなるか確認する（削除確認画面が再表示されるが、「削除する」を押すと 500 エラーになる。存在しない ID を削除しようとするため。この対処は今後の応用として考えてみよう）

---

### 問題 05-4：検索フォームの骨格を追加する

**目標**: 一覧画面に検索フォームを追加し、入力した値がフォームに保持されるようにする。  
実際の絞り込みは次章で実装します。

**編集するファイル**
- `controller/EmployeeController.java`（`index` に `@RequestParam` を追加）
- `templates/employee/index.html`（検索フォームを追加）

#### ステップ 1：Controller の index に @RequestParam を追加する

```java
// import に追加
import org.springframework.web.bind.annotation.RequestParam;

// index メソッドを変更
@GetMapping
public String index(@RequestParam(defaultValue = "") String keyword, Model model) {
    List<Employee> employees = employeeRepository.findAll();
    model.addAttribute("employees", employees);
    model.addAttribute("keyword", keyword);
    return "employee/index";
}
```

> **ポイント**: `findAll()` の SQL はまだ変更しません。  
> キーワードを受け取るだけで、絞り込みは ch06 で実装します。

#### ステップ 2：index.html に検索フォームを追加する

「新規登録」ボタンの下、件数表示の上に追加する。

```html
<form method="get" class="mb-3">
    <div class="d-flex gap-2" style="max-width: 500px;">
        <input type="text" name="keyword" th:value="${keyword}"
               class="form-control" placeholder="氏名で検索..." />
        <button type="submit" class="btn btn-outline-secondary">検索</button>
    </div>
</form>
```

> **ポイント**: `th:value="${keyword}"` で検索後も入力値がフォームに残ります。  
> フォームの `method="get"` は URL に `?keyword=田中` を付けて送信します。

**確認ポイント**
- 検索フォームが表示されること
- フォームに「田中」と入力して「検索」を押すと URL が `/employees?keyword=田中` に変わること
- （まだ絞り込みは動かない。田中以外の社員も表示されたままで OK）
- 検索後もフォームに「田中」が入力された状態のままであること
