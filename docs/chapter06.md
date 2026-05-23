# ch06 絞り込み・ソート

## このチャプターで学ぶこと

- クエリパラメータ（`?keyword=田中`）を使ったキーワード検索の実装
- `WHERE 1=1` を起点にした動的 WHERE 句の組み立て方
- ユーザー入力を ORDER BY に使うときの SQL インジェクション対策
- Thymeleaf で複数クエリパラメータを持つリンクを生成する方法

---

## 基礎知識

### @RequestParam のおさらい

```java
@GetMapping
public String index(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(required = false) Integer deptId,
        Model model) {
    // keyword: /employees → "" 、/employees?keyword=田中 → "田中"
    // deptId:  /employees → null、/employees?deptId=1 → 1
}
```

| 属性 | 説明 |
|---|---|
| `defaultValue = "..."` | パラメータがないときのデフォルト値 |
| `required = false` | パラメータが省略可能（省略時は `null`） |

### 動的 WHERE 句の組み立て — WHERE 1=1 パターン

検索条件がある場合とない場合を両方対応するとき、SQL を動的に組み立てます。  
`WHERE 1=1` を起点にすると、条件の有無にかかわらず `AND` でつなぐだけで済みます。

```java
// 例：keyword があれば LIKE 検索、なければ全件取得
StringBuilder sql = new StringBuilder(
    "SELECT id, name FROM employees WHERE 1=1"
);
MapSqlParameterSource params = new MapSqlParameterSource();

if (keyword != null && !keyword.isBlank()) {
    sql.append(" AND name LIKE :keyword");
    params.addValue("keyword", "%" + keyword + "%");
}

// keyword が "" なら → SELECT ... WHERE 1=1
// keyword が "田中" なら → SELECT ... WHERE 1=1 AND name LIKE '%田中%'
```

> **ポイント**: `LIKE '%田中%'` は「田中を含む」という部分一致です。  
> tokkun-sql の ch02 で学んだ LIKE 演算子と同じです。

### ORDER BY への SQL インジェクション対策

ORDER BY の列名はプレースホルダ（`:param`）が使えません。  
ユーザーが入力した値をそのまま SQL 文字列に組み込むと**SQL インジェクション**の危険があります。

```java
// ❌ 危険：ユーザー入力をそのまま使う
sql.append(" ORDER BY " + sortBy);  // sortBy に "id; DROP TABLE employees" など入れられる

// ✅ 安全：許可リスト（ホワイトリスト）で検証する
Set<String> SORTABLE = Set.of("name", "salary", "hire_date");
String column = SORTABLE.contains(sortBy) ? sortBy : "hire_date";
sql.append(" ORDER BY ").append(column).append(" ").append(direction);
```

`Set.contains()` でホワイトリストに含まれる列名のみ使用し、不正な値はデフォルトにフォールバックします。

### Thymeleaf での複数パラメータ URL

`@{...}` に `(param=value, ...)` を追加すると、クエリパラメータ付きの URL を生成できます。

```html
<!-- /employees?keyword=田中&sortBy=salary&sortDir=asc -->
<a th:href="@{/employees(keyword=${keyword}, sortBy='salary', sortDir='asc')}">給与</a>
```

条件式を使ってソート方向を切り替えることもできます：

```html
<!-- 現在 salary でソートかつ昇順のときは 'desc'、それ以外は 'asc' -->
<a th:href="@{/employees(keyword=${keyword},
                         sortBy='salary',
                         sortDir=${sortBy == 'salary' and sortDir == 'asc' ? 'desc' : 'asc'}
                        )}">給与</a>
```

> **ポイント**: Thymeleaf では `&&` の代わりに `and` を使います。

---

## スターターコードの確認

現在の状態を確認しましょう。

**動いていること**
- 検索フォームが表示される
- フォームに入力して送信すると URL が `/employees?keyword=田中` に変わる
- フォームに入力した値が検索後も残る

**まだ動いていないこと**
- 検索しても絞り込みが機能しない（全件表示のまま）
- ソートリンクがない

確認すべきファイル：

```
controller/EmployeeController.java   ← keyword を受け取るが findAll() に渡していない
repository/EmployeeRepository.java  ← findAll() がパラメータなし・条件なし
templates/employee/index.html        ← th には keyword のみ・ソートリンクなし
```

---

## 練習問題

### 問題 06-1：キーワード検索を実装する

**目標**: 検索フォームに入力した氏名で一覧を絞り込めるようにする。

**編集するファイル**
- `repository/EmployeeRepository.java`（動的 WHERE を実装）
- `controller/EmployeeController.java`（keyword を findAll() に渡す）

#### ステップ 1：findAll() のシグネチャを変更する

`import` に追加する：

```java
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import java.util.Set;
```

`findAll` メソッドを書き換える：

```java
public List<Employee> findAll(String keyword) {
    StringBuilder sql = new StringBuilder(
        "SELECT id, name, dept_id, salary, hire_date, manager_id FROM employees WHERE 1=1"
    );
    MapSqlParameterSource params = new MapSqlParameterSource();

    if (keyword != null && !keyword.isBlank()) {
        sql.append(" AND name LIKE :keyword");
        params.addValue("keyword", "%" + keyword + "%");
    }

    sql.append(" ORDER BY hire_date DESC");

    return jdbcTemplate.query(sql.toString(), params, new BeanPropertyRowMapper<>(Employee.class));
}
```

#### ステップ 2：Controller から findAll(keyword) を呼ぶ

```java
@GetMapping
public String index(@RequestParam(defaultValue = "") String keyword, Model model) {
    List<Employee> employees = employeeRepository.findAll(keyword);  // ← keyword を渡す
    model.addAttribute("employees", employees);
    model.addAttribute("keyword", keyword);
    return "employee/index";
}
```

**確認ポイント**
- 検索フォームに「田中」と入力して検索すると、田中を含む社員だけが表示されること
- フォームを空にして検索すると全件表示に戻ること
- 検索後もフォームに入力値が残ること

---

### 問題 06-2：部署 ID による絞り込みを追加する

**目標**: 検索フォームに部署 ID フィールドを追加し、部署 ID でも絞り込めるようにする。

**編集するファイル**
- `repository/EmployeeRepository.java`（deptId 条件を追加）
- `controller/EmployeeController.java`（deptId パラメータを追加）
- `templates/employee/index.html`（フォームに deptId 入力欄を追加）

#### ステップ 1：findAll() に deptId を追加する

```java
public List<Employee> findAll(String keyword, Integer deptId) {
    // ... （keyword の処理に続けて追加）
    if (deptId != null) {
        sql.append(" AND dept_id = :deptId");
        params.addValue("deptId", deptId);
    }
    // ...
}
```

#### ステップ 2：Controller に @RequestParam deptId を追加する

```java
@GetMapping
public String index(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(required = false) Integer deptId,
        Model model) {
    List<Employee> employees = employeeRepository.findAll(keyword, deptId);
    model.addAttribute("employees", employees);
    model.addAttribute("keyword", keyword);
    model.addAttribute("deptId", deptId);
    return "employee/index";
}
```

#### ステップ 3：index.html のフォームに部署 ID 入力欄を追加する

```html
<form method="get" class="mb-3">
    <div class="d-flex gap-2" style="max-width: 600px;">
        <input type="text" name="keyword" th:value="${keyword}"
               class="form-control" placeholder="氏名で検索..." />
        <input type="number" name="deptId" th:value="${deptId}"
               class="form-control" style="max-width: 100px;" placeholder="部署ID" />
        <button type="submit" class="btn btn-outline-secondary">検索</button>
    </div>
</form>
```

**確認ポイント**
- 部署 ID に「1」を入力して検索すると、部署 ID が 1 の社員だけが表示されること
- 氏名と部署 ID を組み合わせて絞り込めること

---

### 問題 06-3：ソート機能を実装する

**目標**: ソート列（sortBy）とソート方向（sortDir）を URL で指定できるようにする。

**編集するファイル**
- `repository/EmployeeRepository.java`（動的 ORDER BY を実装）
- `controller/EmployeeController.java`（sortBy・sortDir パラメータを追加）

#### ステップ 1：findAll() に sortBy と sortDir を追加する

```java
public List<Employee> findAll(String keyword, Integer deptId, String sortBy, String sortDir) {
    // ホワイトリストで SQL インジェクションを防ぐ
    Set<String> sortableColumns = Set.of("name", "salary", "hire_date");
    String column    = sortableColumns.contains(sortBy) ? sortBy : "hire_date";
    String direction = "asc".equals(sortDir) ? "ASC" : "DESC";

    StringBuilder sql = new StringBuilder(
        "SELECT id, name, dept_id, salary, hire_date, manager_id FROM employees WHERE 1=1"
    );
    MapSqlParameterSource params = new MapSqlParameterSource();

    if (keyword != null && !keyword.isBlank()) {
        sql.append(" AND name LIKE :keyword");
        params.addValue("keyword", "%" + keyword + "%");
    }
    if (deptId != null) {
        sql.append(" AND dept_id = :deptId");
        params.addValue("deptId", deptId);
    }

    sql.append(" ORDER BY ").append(column).append(" ").append(direction);

    return jdbcTemplate.query(sql.toString(), params, new BeanPropertyRowMapper<>(Employee.class));
}
```

#### ステップ 2：Controller に sortBy・sortDir を追加する

```java
@GetMapping
public String index(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(required = false) Integer deptId,
        @RequestParam(defaultValue = "hire_date") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        Model model) {
    List<Employee> employees = employeeRepository.findAll(keyword, deptId, sortBy, sortDir);
    model.addAttribute("employees", employees);
    model.addAttribute("keyword", keyword);
    model.addAttribute("deptId", deptId);
    model.addAttribute("sortBy", sortBy);
    model.addAttribute("sortDir", sortDir);
    return "employee/index";
}
```

**確認ポイント**
- ブラウザで `/employees?sortBy=salary&sortDir=asc` にアクセスすると給与の昇順で表示されること
- `/employees?sortBy=name&sortDir=asc` にアクセスすると氏名の昇順で表示されること

---

### 問題 06-4：カラムヘッダーにソートリンクを追加する

**目標**: テーブルの「氏名」「給与」「入社日」列のヘッダーをクリックするとその列でソートされ、  
同じ列を再度クリックすると昇順↔降順が切り替わるようにする。

**編集するファイル**
- `templates/employee/index.html`

**手順のヒント**

ヘッダー行を以下のように書き換えます。  
`sortBy == '列名' and sortDir == 'asc' ? 'desc' : 'asc'` が方向の切り替えロジックです。

```html
<thead>
    <tr>
        <th>ID</th>
        <th>
            <a th:href="@{/employees(keyword=${keyword},deptId=${deptId},sortBy='name',
                        sortDir=${sortBy == 'name' and sortDir == 'asc' ? 'desc' : 'asc'})}">氏名</a>
        </th>
        <th>部署 ID</th>
        <th>
            <a th:href="@{/employees(keyword=${keyword},deptId=${deptId},sortBy='salary',
                        sortDir=${sortBy == 'salary' and sortDir == 'asc' ? 'desc' : 'asc'})}">給与</a>
        </th>
        <th>
            <a th:href="@{/employees(keyword=${keyword},deptId=${deptId},sortBy='hire_date',
                        sortDir=${sortBy == 'hire_date' and sortDir == 'asc' ? 'desc' : 'asc'})}">入社日</a>
        </th>
        <th>上司 ID</th>
        <th></th>
    </tr>
</thead>
```

> **ポイント**: `keyword=${keyword}` を含めることで、検索中にソートを変更しても検索条件がリセットされません。

**確認ポイント**
- 「給与」ヘッダーをクリックすると給与の降順になること
- もう一度「給与」をクリックすると昇順に切り替わること
- 「田中」で検索した状態でソートリンクをクリックしても、検索条件が維持されること
