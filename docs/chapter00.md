# ch00 はじめに

この章では、コードを書く前に必要な「Web アプリケーションの仕組み」を学びます。
演習問題はありません。図と説明を読んで全体像をつかんでください。

---

## 1. Web アプリケーションとは

ブラウザで URL を開くと、画面が表示されます。この裏側では何が起きているのでしょうか。

```
【ブラウザ（クライアント）】              【サーバー】
        |                                      |
        |  ① HTTP リクエスト（GET /employees）  |
        |------------------------------------->|
        |                                      |  ② DB に問い合わせ
        |                                      |  ← SELECT * FROM employees
        |                                      |  → 結果（行データ）
        |                                      |
        |  ③ HTTP レスポンス（HTML）            |
        |<-------------------------------------|
        |                                      |
  ④ HTML を表示
```

1. **ブラウザ**が「この URL のページをください」とサーバーにリクエストを送る
2. **サーバー**が DB にデータを問い合わせ、HTML を組み立てる
3. **HTML をレスポンス**としてブラウザに返す
4. **ブラウザ**が HTML を解釈して画面に表示する

---

## 2. HTTP リクエスト・レスポンスの基本

### GET リクエスト

ブラウザのアドレスバーに URL を入力して Enter を押すと、**GET リクエスト**が送られます。

```
GET /employees HTTP/1.1
Host: localhost:8080
```

GET は「データを取得したい」という意味です。ページを表示するときは基本的に GET を使います。

### HTTP レスポンス

サーバーはリクエストを受け取ると、**ステータスコード**と一緒に HTML を返します。

| ステータスコード | 意味 |
|-----------------|------|
| 200 OK | 正常にページが返せた |
| 302 Found | 別の URL にリダイレクト（転送）する |
| 404 Not Found | 指定されたページが存在しない |
| 500 Internal Server Error | サーバー側でエラーが発生した |

---

## 3. MVC パターンの役割

この教材では **MVC パターン** という設計を使います。
MVC は **Model**・**View**・**Controller** の頭文字です。

```
ブラウザ
  │  GET /employees
  ▼
┌──────────────┐
│  Controller  │  ← リクエストを受け取る・処理の司令塔
│ (Java クラス) │
└──────┬───────┘
       │ findAll() を呼ぶ
       ▼
┌──────────────┐
│    Model     │  ← データの入れ物（Employee クラスなど）
│ (Java クラス) │
└──────┬───────┘
       │ SQL を実行
       ▼
┌──────────────┐
│  データベース  │
│ (PostgreSQL) │
└──────┬───────┘
       │ 結果を返す
       ▼
┌──────────────┐
│     View     │  ← HTML に変換する（Thymeleaf テンプレート）
│  (.html ファイル) │
└──────────────┘
       │
       ▼
    ブラウザ
```

### それぞれの役割

| レイヤー | ファイルの場所 | 役割 |
|----------|--------------|------|
| **Controller** | `controller/EmployeeController.java` | URL とメソッドを対応付ける。Repository を呼んでデータを取得し、View に渡す |
| **Repository** | `repository/EmployeeRepository.java` | JdbcTemplate で SQL を実行し、結果を返す |
| **Model** | `model/Employee.java` | DB のテーブルの 1 行分のデータを入れておく入れ物 |
| **View** | `templates/employee/index.html` | Controller から渡されたデータを使って HTML を組み立てる |

> **ポイント**: Controller は Repository を通じてデータを取得します。  
> Controller が直接 SQL を書くことはありません。

---

## 4. プロジェクト構成

```
src/EmployeeApp/
└── src/main/
    ├── java/com/example/employeeapp/
    │   ├── EmployeeAppApplication.java   ← アプリの起動クラス（触らない）
    │   ├── controller/
    │   │   └── EmployeeController.java   ← ★ 学習者が触る
    │   ├── model/
    │   │   ├── Employee.java             ← ★ 学習者が触る
    │   │   └── Department.java           ← ★ 学習者が触る
    │   └── repository/
    │       └── EmployeeRepository.java   ← ★ 学習者が触る
    └── resources/
        ├── application.properties        ← DB 接続設定など（触らない）
        └── templates/
            ├── layout/
            │   └── default.html          ← 共通レイアウト（触らない）
            └── employee/
                └── index.html            ← ★ 学習者が触る
```

### 学習者が触るファイル

| ファイル | 何をするか |
|----------|-----------|
| `controller/EmployeeController.java` | URL を受け取り、Repository を呼び、View にデータを渡す |
| `model/Employee.java` | DB の `employees` テーブルの 1 行分のデータを持つ |
| `model/Department.java` | DB の `departments` テーブルの 1 行分のデータを持つ |
| `repository/EmployeeRepository.java` | SQL を書いて DB と通信する |
| `templates/employee/*.html` | 画面の HTML を組み立てる |

### 触らないファイル

| ファイル | 理由 |
|----------|------|
| `EmployeeAppApplication.java` | アプリの起動設定。変更不要 |
| `application.properties` | DB の接続先などの設定。環境依存のため |
| `build.gradle` | ライブラリの依存関係。変更不要 |
| `templates/layout/default.html` | ナビバー・Bootstrap の共通レイアウト |

---

## 5. JdbcTemplate と SQL の接続

tokkun-sql で学んだ SQL は、このアプリでもそのまま使えます。

### tokkun-sql での書き方（SQL ファイル）

```sql
SELECT id, name, salary
FROM employees
ORDER BY hire_date DESC;
```

### このアプリでの書き方（Java）

```java
// repository/EmployeeRepository.java
public List<Employee> findAll() {
    return jdbcTemplate.query(
        "SELECT id, name, salary FROM employees ORDER BY hire_date DESC",
        Map.of(),
        new BeanPropertyRowMapper<>(Employee.class)
    );
}
```

SQL 部分（`"SELECT id, name, salary FROM employees ORDER BY hire_date DESC"`）は  
**tokkun-sql とまったく同じ書き方**です。

`BeanPropertyRowMapper` は SELECT の結果（行データ）を自動的に `Employee` クラスのフィールドに入れてくれます。

```
SELECT の結果                Employee クラス
─────────────────            ─────────────────
id       = 1          →      employee.id       = 1
name     = 田中 太郎   →      employee.name     = "田中 太郎"
salary   = 60000      →      employee.salary   = 60000
hire_date = 2015-04-01 →     employee.hireDate = 2015-04-01
```

> **ポイント**: SQL の列名 `hire_date`（スネークケース）は  
> Java のフィールド名 `hireDate`（キャメルケース）に自動変換されます。

---

## まとめ

| キーワード | 意味 |
|-----------|------|
| HTTP GET | ブラウザがページを要求するときに使うリクエスト方式 |
| MVC | アプリを Controller・Model・View の 3 つに分ける設計パターン |
| Controller | URL を受け取り、処理を調整して View にデータを渡す役割 |
| Model | DB のデータを Java オブジェクトとして表す入れ物 |
| View | Controller から受け取ったデータを HTML に変換する |
| Repository | SQL を実行して DB とやりとりする役割 |
| JdbcTemplate | Java から SQL を実行するための Spring の仕組み |

次の ch01 から、実際にコードを書いて動かしていきます。
