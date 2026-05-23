# ch03 新規登録

## このチャプターで学ぶこと

- GET と POST の違い・使い分け
- HTML フォームと `method="post"` の仕組み
- `@PostMapping` と `@ModelAttribute` でフォーム送信を受け取る方法
- JdbcTemplate で INSERT を実行し、採番された ID を取得する方法
- PRG パターン（Post-Redirect-Get）でページの二重送信を防ぐ方法

---

## 基礎知識

### GET と POST の違い

| | GET | POST |
|---|---|---|
| 用途 | データを取得する（画面を表示する） | データを送信する（登録・更新・削除） |
| URL | クエリパラメータに値が見える（`?name=田中`） | リクエストの本文（ボディ）に値が入る |
| ブラウザの再読み込み | 安全（同じ画面を再表示するだけ） | 危険（同じリクエストを再送信してしまう） |
| アノテーション | `@GetMapping` | `@PostMapping` |

```
GET /employees/new
  → フォームの画面を表示するだけ

POST /employees
  → フォームの内容をサーバーに送って、INSERT を実行する
```

### HTML フォームの仕組み

```html
<form th:action="@{/employees}" method="post">
    <input type="text" name="name" />
    <button type="submit">登録</button>
</form>
```

- `action` は送信先 URL、`method` は HTTP メソッドを指定します
- `<input>` の `name` 属性が、サーバー側で受け取るパラメータ名になります
- 「登録」ボタンを押すと `POST /employees` が送信されます

### @PostMapping と @ModelAttribute

`@PostMapping` は「このメソッドが POST リクエストを処理する」ことを示します。  
`@ModelAttribute` は「フォームの入力値をオブジェクトに自動でまとめて受け取る」アノテーションです。

```java
@PostMapping
public String create(@ModelAttribute Employee employee) {
    // フォームの name="name" → employee.getName()
    // フォームの name="salary" → employee.getSalary()
    // フォームの name="hireDate" → employee.getHireDate()
    // のように自動でマッピングされる
}
```

`name` 属性（HTML）と Java のフィールド名が一致していれば、Spring が自動でセットします。

### PRG パターン（Post-Redirect-Get）

POST の後にそのままページを表示すると、ブラウザの更新ボタンで同じ登録が繰り返されてしまいます。  
これを防ぐのが **PRG パターン** です。

```
① ユーザーがフォームを送信（POST /employees）
        ↓
② サーバーが INSERT を実行
        ↓
③ サーバーが「302 リダイレクト」を返す（GET /employees/3 へ移動して）
        ↓
④ ブラウザが GET /employees/3 にアクセス（詳細画面を表示）
        ↓
⑤ ブラウザを更新しても GET /employees/3 が再実行されるだけ（安全）
```

Spring MVC でリダイレクトするには `"redirect:/path"` を返します。

```java
// 一覧にリダイレクト
return "redirect:/employees";

// ID=3 の詳細にリダイレクト
return "redirect:/employees/3";
```

### JdbcTemplate での INSERT と採番 ID 取得

tokkun-sql で学んだ INSERT 文をそのまま使います。

```sql
-- tokkun-sql で書いた INSERT
INSERT INTO employees (name, salary, hire_date)
VALUES ('田中 太郎', 60000, '2023-04-01');
```

Java では以下のように書きます。

```java
// INSERT して生成された ID を受け取るための KeyHolder
KeyHolder keyHolder = new GeneratedKeyHolder();

// パラメータを MapSqlParameterSource でまとめる
MapSqlParameterSource params = new MapSqlParameterSource()
    .addValue("name",     employee.getName())
    .addValue("salary",   employee.getSalary())
    .addValue("hireDate", employee.getHireDate());

// INSERT を実行。第 3 引数が KeyHolder、第 4 引数が取得したい列名
jdbcTemplate.update(
    "INSERT INTO employees (name, salary, hire_date) VALUES (:name, :salary, :hireDate)",
    params,
    keyHolder,
    new String[]{"id"}
);

// 生成された ID を取得
int newId = keyHolder.getKey().intValue();
```

> **ポイント**: `new String[]{"id"}` で「`id` 列の自動採番値を取得したい」と JDBC ドライバーに伝えています。  
> `Map.of(...)` ではなく `MapSqlParameterSource` を使うのは、`KeyHolder` の引数に `SqlParameterSource` が必要なためです。

### LocalDate のフォーム変換

HTML の `<input type="date">` は日付を `YYYY-MM-DD` 形式（例：`2023-04-01`）で送信します。  
Spring がこの文字列を `LocalDate` に変換するには、フィールドに `@DateTimeFormat` アノテーションが必要です。

```java
// model/Employee.java
import org.springframework.format.annotation.DateTimeFormat;

@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
private LocalDate hireDate;
```

---

## スターターコードの確認

現在の状態を確認しましょう。

**動いていること**
- `GET /employees/new` でフォームが表示される（氏名・給与のみ）
- 「キャンセル」で一覧に戻れる

**まだ動いていないこと**
- フォームを送信（POST）しても 405 エラーになる（POST ハンドラがない）
- 入社日・部署 ID の入力欄がない

確認すべきファイル：

```
controller/EmployeeController.java   ← createForm メソッドはあるが create メソッドがない
repository/EmployeeRepository.java  ← save メソッドがない
templates/employee/create.html      ← name と salary のフィールドだけ
```

---

## 練習問題

### 問題 03-1：フォームに「入社日」と「部署 ID」を追加する

**目標**: 登録フォームに「入社日」と「部署 ID」の入力欄を追加する。  
あわせて、日付の変換が正しく動くよう `Employee.java` にアノテーションを追加する。

**編集するファイル**
- `templates/employee/create.html`
- `model/Employee.java`

#### ステップ 1：create.html にフィールドを追加する

```html
<!-- 給与フィールドの下に追加 -->
<div class="mb-3">
    <label class="form-label">部署 ID</label>
    <input type="number" name="deptId" class="form-control" />
</div>
<div class="mb-3">
    <label class="form-label">入社日</label>
    <input type="date" name="hireDate" class="form-control" />
</div>
```

> **ポイント**: `name="hireDate"` と書くと `employee.hireDate`（Java のフィールド名）に対応します。  
> HTML の日付入力（`type="date"`）は `YYYY-MM-DD` 形式で値を送信します。

#### ステップ 2：Employee.java に @DateTimeFormat を追加する

```java
// model/Employee.java
import org.springframework.format.annotation.DateTimeFormat;

// hireDate フィールドの定義の上に追加
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
private LocalDate hireDate;
```

**確認ポイント**
- `GET /employees/new` を開いて入社日と部署 ID の入力欄が表示されること
- （まだ送信しても失敗します。次の問題で実装します）

---

### 問題 03-2：Repository に INSERT メソッドを実装する

**目標**: `EmployeeRepository` に `save` メソッドを追加し、フォームから送られたデータを INSERT する。  
INSERT 後に生成された ID を戻り値として返すこと。

**編集するファイル**
- `repository/EmployeeRepository.java`

**手順のヒント**

ファイルの先頭の `import` に以下を追加します。

```java
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
```

`findById` の後に `save` メソッドを追加します。

```java
public int save(Employee employee) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("name",     employee.getName())
        .addValue("deptId",   employee.getDeptId())
        .addValue("salary",   employee.getSalary())
        .addValue("hireDate", employee.getHireDate());
    jdbcTemplate.update(
        "INSERT INTO employees (name, dept_id, salary, hire_date)" +
        " VALUES (:name, :deptId, :salary, :hireDate)",
        params,
        keyHolder,
        new String[]{"id"}
    );
    return keyHolder.getKey().intValue();
}
```

**確認ポイント**
- ビルドエラーが出ないこと（`./gradlew build` で確認）
- （まだコントローラーから呼び出していないので、画面の動作は変わりません）

---

### 問題 03-3：POST アクションを実装して登録後に一覧へリダイレクト

**目標**: `EmployeeController` に `create` メソッド（POST 処理）を追加し、INSERT を実行して一覧へリダイレクトする。

**編集するファイル**
- `controller/EmployeeController.java`

**手順のヒント**

`import` に以下を追加します。

```java
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
```

`createForm` の後に `create` メソッドを追加します。

```java
@PostMapping
public String create(@ModelAttribute Employee employee) {
    employeeRepository.save(employee);
    return "redirect:/employees";
}
```

**確認ポイント**
- `/employees/new` でフォームに値を入力して「登録」を押すと一覧画面に戻ること
- 一覧に新しく登録した社員が表示されること
- ブラウザを更新（F5）しても再登録されないこと（PRG パターンの確認）

---

### 問題 03-4：登録後のリダイレクト先を詳細画面に変更する

**目標**: 登録後に一覧ではなく、登録した社員の詳細画面（`/employees/{id}`）へリダイレクトする。

**編集するファイル**
- `controller/EmployeeController.java`

**手順のヒント**

`save` メソッドは生成された ID を返します。その ID を使ってリダイレクト先を変更します。

```java
@PostMapping
public String create(@ModelAttribute Employee employee) {
    int id = employeeRepository.save(employee);
    return "redirect:/employees/" + id;
}
```

**確認ポイント**
- 「登録」を押すと登録した社員の詳細画面が表示されること
- 詳細画面に入力した氏名・給与・入社日が表示されること

---

### 問題 03-5：編集フォームの土台を作る

**目標**: `GET /employees/3/edit` にアクセスすると、ID=3 の社員の情報が入力済みの編集フォームが表示されるようにする。  
更新の保存（POST）は次章で実装します。

**編集・追加するファイル**
- `controller/EmployeeController.java`（`editForm` メソッドを追加）
- `templates/employee/edit.html`（新規作成）
- `templates/employee/detail.html`（「編集」リンクを追加）
- `templates/employee/index.html`（「編集」リンクを追加）

#### ステップ 1：Controller に editForm アクションを追加する

```java
@GetMapping("/{id}/edit")
public String editForm(@PathVariable int id, Model model) {
    Employee employee = employeeRepository.findById(id);
    model.addAttribute("employee", employee);
    return "employee/edit";
}
```

#### ステップ 2：edit.html を新規作成する

`create.html` と同じ構造ですが、各フィールドに `th:value` で既存の値を表示します。

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/default}">
<head>
    <title>社員編集</title>
</head>
<body>
<div layout:fragment="content">
    <h1>社員編集</h1>

    <form th:action="@{/employees/{id}/edit(id=${employee.id})}" method="post">
        <input type="hidden" name="id" th:value="${employee.id}" />
        <div class="mb-3">
            <label class="form-label">氏名</label>
            <input type="text" name="name" th:value="${employee.name}" class="form-control" />
        </div>
        <div class="mb-3">
            <label class="form-label">部署 ID</label>
            <input type="number" name="deptId" th:value="${employee.deptId}" class="form-control" />
        </div>
        <div class="mb-3">
            <label class="form-label">給与</label>
            <input type="number" name="salary" th:value="${employee.salary}" class="form-control" />
        </div>
        <div class="mb-3">
            <label class="form-label">入社日</label>
            <input type="date" name="hireDate" th:value="${employee.hireDate}" class="form-control" />
        </div>
        <button type="submit" class="btn btn-primary">更新</button>
        <a th:href="@{/employees/{id}(id=${employee.id})}" class="btn btn-secondary">キャンセル</a>
    </form>
</div>
</body>
</html>
```

> **ポイント**: `th:value` は `<input>` に初期値を設定します。  
> `create.html` の `<input>` には `value` がなかったので空欄でしたが、  
> `edit.html` では既存のデータが入った状態でフォームが表示されます。
>
> `<input type="hidden" name="id" ...>` は ID を POST リクエストに含めるために必要です。

#### ステップ 3：detail.html に「編集」リンクを追加する

```html
<!-- 「一覧に戻る」リンクの前に追加 -->
<a th:href="@{/employees/{id}/edit(id=${employee.id})}" class="btn btn-secondary">編集</a>
```

#### ステップ 4：index.html の一覧に「編集」リンクを追加する

```html
<!-- 詳細リンクの隣に追加 -->
<td>
    <a th:href="@{/employees/{id}(id=${employee.id})}">詳細</a>
    <a th:href="@{/employees/{id}/edit(id=${employee.id})}">編集</a>
</td>
```

**確認ポイント**
- `/employees/1/edit` にアクセスすると田中 太郎の情報が入力済みのフォームが表示されること
- 詳細画面と一覧画面に「編集」リンクが表示されること
- 「キャンセル」をクリックすると詳細画面に戻ること
- 「更新」ボタンを押すと 405 エラーになること（POST ハンドラは次章で実装）
