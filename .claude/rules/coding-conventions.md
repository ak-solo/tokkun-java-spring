---
paths:
  - "src/**"
  - "db/**"
---

# コーディング規約・スターターコードの方針

## コーディング規約（教材内コードの統一ルール）

### Java
- クラス名は PascalCase（`EmployeeController`・`EmployeeRepository`）
- メソッド名・変数名は camelCase（`findAll`・`employeeList`）
- 定数は UPPER_SNAKE_CASE（`MAX_SALARY`）
- パッケージ名は小文字（`com.example.employeeapp.controller`）
- アノテーションはクラス・メソッド定義の直前に記述する

### SQL
- 予約語は大文字（tokkun-sql に準拠）
- 例：`SELECT e.id, e.name FROM employees e WHERE e.id = :id`

### Thymeleaf
- テンプレートは Bootstrap 5 で最低限のスタイルのみ。CSS は深く触れない
- 変数展開は `th:text="${variable}"` を使う（インライン記法 `[[${...}]]` は避ける）
- フォームは `th:action`・`th:object`・`th:field` を使う

### バリデーション
- Bean Validation（`jakarta.validation`）を使う（ch04 以降）
- アノテーション例：`@NotBlank`・`@NotNull`・`@Min`・`@Max`・`@Size`・`@PastOrPresent`
- コントローラで `@Valid` + `BindingResult` を受け取り、エラー時はフォーム画面に戻す
- エラーメッセージは `th:errors="*{fieldName}"` で表示する

### その他
- 非同期処理は扱わない（`CompletableFuture` 等は対象外）
- エラーハンドリングは最低限（例外キャッチは扱わない。入力検証のみ）
- `@Autowired` フィールドインジェクションは使わず、コンストラクタインジェクションに統一する

## レイヤー構成

```
controller/   ← リクエストの受け取り・レスポンスの返却
repository/   ← JdbcTemplate を使った SQL の実行
model/        ← DB のカラムに対応するフィールドを持つ POJO
templates/    ← Thymeleaf テンプレート
```

- Controller は Repository を通じてデータを取得し、Model に詰めて View に渡す
- Controller が直接 JdbcTemplate を扱わない（Repository に委譲する）
- Model は getter/setter を持つ単純な POJO とする（ビジネスロジックは持たせない）

## JdbcTemplate の使い方

- `NamedParameterJdbcTemplate` を使い、プレースホルダは `:paramName` 形式で書く（`?` は使わない）
- パラメータは `MapSqlParameterSource` または `Map.of(...)` で渡す
- 1件取得は `queryForObject`・複数件は `query` を使う
- INSERT 後に採番 ID を取得する場合は `KeyHolder` を使う

```java
// 例：1件取得
Employee employee = jdbcTemplate.queryForObject(
    "SELECT * FROM employees WHERE id = :id",
    Map.of("id", id),
    new BeanPropertyRowMapper<>(Employee.class)
);

// 例：一覧取得
List<Employee> employees = jdbcTemplate.query(
    "SELECT * FROM employees ORDER BY id",
    new BeanPropertyRowMapper<>(Employee.class)
);
```

## スターターコードの方針

- `src/` 以下に動作する状態の Spring Boot アプリを用意する
- DB マイグレーション・シードデータは `db/` 以下に SQL ファイルとして置く
- 学習者が触るファイルは `controller/`・`model/`・`templates/` のみ
  （`Application.java`・`application.properties`・`build.gradle` 等は触らない）
- フェーズ 1 の各章ごとにスターターブランチ or タグを切る（`ch01-start` 等）
