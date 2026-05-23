# ch07 新画面作成（一覧＋詳細）

## このチャプターで学ぶこと

- Controller / Repository / Model / View を**ゼロから新規作成**する
- `employee_projects` を JOIN してプロジェクトの参加人数を一覧に表示する
- `projects` と `employee_projects` と `employees` を JOIN して詳細画面に参加メンバー一覧を表示する
- `ch01〜ch06` で身に付けたパターンを組み合わせて、仕様書をもとに自力で実装する

---

## 前提

- スターターコードは提供しない
- `employees` 画面で実装したコード（`EmployeeController`・`EmployeeRepository`・各テンプレート）を参考にしながら実装すること
- 詰まったときは [ヒント](#ヒント) を参照すること

---

## 作成する画面の仕様

### 画面 1：プロジェクト一覧（`/projects`）

#### URL

| メソッド | パス | 説明 |
|---------|------|------|
| GET | `/projects` | プロジェクト一覧画面 |

#### 表示項目

テーブルに以下の列を表示すること。

| 列 | 取得元 | 補足 |
|----|--------|------|
| ID | `projects.id` | |
| プロジェクト名 | `projects.name` | |
| 開始日 | `projects.start_date` | |
| 終了日 | `projects.end_date` | `null` の場合は「進行中」と表示する |
| 予算 | `projects.budget` | |
| 参加人数 | `employee_projects` の件数 | `COUNT(ep.employee_id)` を使う |
| 詳細リンク | — | 各行に「詳細」リンクを表示する |

#### ソート

- デフォルトのソート順：開始日の新しい順（`start_date DESC`）
- 以下の列のヘッダーをクリックするとソートできること：
  - プロジェクト名（`name`）
  - 予算（`budget`）
  - 開始日（`start_date`）
- 同じ列を再度クリックすると昇順↔降順が切り替わること

---

### 画面 2：プロジェクト詳細（`/projects/{id}`）

#### URL

| メソッド | パス | 説明 |
|---------|------|------|
| GET | `/projects/{id}` | プロジェクト詳細画面 |

#### 表示項目

**プロジェクト情報**

| 項目 | 取得元 | 補足 |
|------|--------|------|
| ID | `projects.id` | |
| プロジェクト名 | `projects.name` | |
| 開始日 | `projects.start_date` | |
| 終了日 | `projects.end_date` | `null` の場合は「進行中」と表示する |
| 予算 | `projects.budget` | |

**参加メンバー一覧**

このプロジェクトに参加している社員をテーブルで表示すること。

| 列 | 取得元 |
|----|--------|
| 社員名 | `employees.name` |
| 役割 | `employee_projects.role` |

---

## 作成するファイル

| ファイル | 説明 |
|--------|------|
| `model/Project.java` | `projects` テーブルのカラム + `memberCount` フィールドを持つ POJO |
| `model/ProjectMember.java` | 参加メンバー（社員名 + 役割）を表す POJO |
| `repository/ProjectRepository.java` | `findAll()` と `findById()` を実装 |
| `controller/ProjectController.java` | `index` と `detail` アクションを実装 |
| `templates/project/index.html` | プロジェクト一覧テンプレート |
| `templates/project/detail.html` | プロジェクト詳細テンプレート |

---

## 確認ポイント

- `/projects` でプロジェクト一覧が表示されること
- 開始日の列ヘッダーをクリックすると昇順↔降順が切り替わること
- 各行の「詳細」リンクをクリックすると `/projects/{id}` に遷移すること
- 詳細画面にプロジェクト情報と参加メンバー一覧が表示されること
- 終了日が `null` のプロジェクトが「進行中」と表示されること

---

## ヒント

詰まったときだけ読むこと。

### Model を作る

`Employee.java` と同じ構造の POJO を新規作成する。  
`projects` テーブルのカラムに加えて、`COUNT()` で取得した参加人数を受け取る `memberCount` フィールドを追加する。

参加メンバー用には、`ProjectMember.java` を別途作成する（フィールドは `name` と `role` のみ）。

### Repository を作る

`EmployeeRepository.java` を参考に `ProjectRepository.java` を新規作成する。

**一覧取得の SQL（ヒント）**

```sql
SELECT p.id, p.name, p.start_date, p.end_date, p.budget,
       COUNT(ep.employee_id) AS member_count
FROM projects p
LEFT JOIN employee_projects ep ON p.id = ep.project_id
GROUP BY p.id, p.name, p.start_date, p.end_date, p.budget
ORDER BY p.start_date DESC
```

- `BeanPropertyRowMapper` で `member_count` を `memberCount` フィールドに自動マッピングされる（スネークケース→キャメルケース変換）
- ソートは `EmployeeRepository.findAll()` と同じホワイトリスト方式で実装する

**詳細取得の SQL（ヒント）**

`findById()` は 2 つのクエリを実行する：
1. プロジェクト情報を 1 件取得する（`queryForObject`）
2. 参加メンバー一覧を取得する（`query`）

参加メンバーの SQL：
```sql
SELECT e.name, ep.role
FROM employee_projects ep
JOIN employees e ON ep.employee_id = e.id
WHERE ep.project_id = :id
```

### Controller を作る

`EmployeeController.java` の `index` と `detail` アクションを参考に `ProjectController.java` を新規作成する。

- クラスに `@RequestMapping("/projects")` をつける
- `findById()` が返すプロジェクト情報とメンバー一覧を、それぞれ別の属性名で `model` に追加する

```java
model.addAttribute("project", project);
model.addAttribute("members", members);
```

### View を作る

`templates/employee/index.html` と `templates/employee/detail.html` を参考に作成する。

終了日の `null` チェックには Thymeleaf の `th:if` / `th:unless` か Elvis 演算子（`?:`）を使う：

```html
<!-- null なら「進行中」、それ以外は日付をそのまま表示 -->
<td th:text="${project.endDate} ?: '進行中'"></td>
```

メンバー一覧は `th:each` で繰り返し表示する：

```html
<tr th:each="member : ${members}">
    <td th:text="${member.name}"></td>
    <td th:text="${member.role}"></td>
</tr>
```

### ナビゲーションにリンクを追加する

`templates/layout/default.html` のナビゲーションバーに「プロジェクト」リンクを追加すると便利。

```html
<a class="nav-link" th:href="@{/projects}">プロジェクト</a>
```
