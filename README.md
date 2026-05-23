# tokkun-java-spring

Spring Boot（Spring MVC）の学習教材です。  
`tokkun-java`（ch01〜10）と `tokkun-sql`（ch00〜11）を修了した方を対象にしています。

---

## 対象者・前提知識

| 教材 | 習得済み想定の知識 |
|------|------------------|
| tokkun-java ch01〜10 | 変数・型・分岐・繰り返し・配列・メソッド・クラス・Stream API |
| tokkun-sql ch00〜11 | SELECT / WHERE / JOIN / GROUP BY / DML（INSERT/UPDATE/DELETE）/ DDL |

---

## 学習目標

1. HTTP リクエスト・レスポンスの基本的な流れを理解する
2. MVC パターン（Model / View / Controller）の役割を理解する
3. 既存の画面に機能追加・仕様変更ができる
4. データの一覧・詳細・登録・更新・削除（CRUD）を自分で実装できる
5. 新しい画面を一から設計・実装できる

---

## 使用技術

| 要素 | 技術 |
|------|------|
| フレームワーク | Spring Boot 3（Spring MVC） |
| テンプレートエンジン | Thymeleaf |
| データアクセス | Spring JDBC（JdbcTemplate） |
| DB | PostgreSQL |
| ビルドツール | Gradle |
| 開発環境 | VS Code + Dev Container |

---

## 環境構築

[docs/setup.md](docs/setup.md) の手順に従って環境を準備してください。

---

## 章一覧

### フェーズ 1：既存画面への機能追加（ch00〜ch06）

各章ごとにスターターコードが提供されます。動く状態の画面に対して、練習問題を解きながら機能を追加していきます。

| 章 | ドキュメント | テーマ | スターター |
|----|-------------|--------|-----------|
| ch00 | [chapter00.md](docs/chapter00.md) | はじめに（Web の仕組み・MVC・プロジェクト構成） | — |
| ch01 | [chapter01.md](docs/chapter01.md) | 一覧表示 | `git checkout ch01-start` |
| ch02 | [chapter02.md](docs/chapter02.md) | 詳細表示 | `git checkout ch02-start` |
| ch03 | [chapter03.md](docs/chapter03.md) | 新規登録 | `git checkout ch03-start` |
| ch04 | [chapter04.md](docs/chapter04.md) | 編集 | `git checkout ch04-start` |
| ch05 | [chapter05.md](docs/chapter05.md) | 削除 | `git checkout ch05-start` |
| ch06 | [chapter06.md](docs/chapter06.md) | 絞り込み・ソート | `git checkout ch06-start` |

### フェーズ 2：新しい画面の作成（ch07〜ch08）

スターターコードはありません。仕様書を読んで、一から自力で実装します。

| 章 | ドキュメント | テーマ |
|----|-------------|--------|
| ch07 | [chapter07.md](docs/chapter07.md) | 新画面作成（一覧＋詳細） |
| ch08 | [chapter08.md](docs/chapter08.md) | 新画面作成（CRUD 完成） |

---

## 学習の進め方

### フェーズ 1（ch01〜ch06）

1. 該当章のスターターコードをチェックアウトする

   ```bash
   git checkout ch01-start   # ch01 の場合
   ```

2. アプリを起動する

   ```bash
   cd src/EmployeeApp
   ./gradlew bootRun
   ```

3. `docs/chapterXX.md` を開き、「スターターコードの確認」で現状を把握する

4. 練習問題を順番に解く。ヒントは各問題に付いているが、まず自力で考えること

5. 「確認ポイント」でブラウザから動作を確認する

### フェーズ 2（ch07〜ch08）

1. ch07-start タグの状態（ch06 解答コード）からスタートする

   ```bash
   git checkout ch07-start
   ```

2. `docs/chapter07.md` の仕様書を読み、画面設計・URL 設計を自分で行う

3. 新規ファイルを作成して実装する（Controller・Repository・Model・View すべて）

4. `docs/chapter08.md` に進み、CRUD を追加して完成させる

---

## データモデル

tokkun-sql と同じ「架空の社内システム」のテーブルを使用します。

```
employees    (id, name, dept_id, salary, hire_date, manager_id)
departments  (id, name, location)
projects     (id, name, start_date, end_date, budget)
employee_projects (employee_id, project_id, role)
```

シードデータは `db/01_seed.sql` に定義されており、コンテナ起動時に自動で投入されます。

---

## ディレクトリ構成

```
tokkun-java-spring/
├── docs/               # 章ドキュメント・練習問題
│   ├── setup.md
│   ├── chapter00.md
│   └── ...
├── db/
│   ├── 00_schema.sql   # テーブル定義
│   └── 01_seed.sql     # サンプルデータ
└── src/EmployeeApp/    # Spring Boot アプリケーション
    └── src/main/
        ├── java/com/example/employeeapp/
        │   ├── controller/   ← 学習者が触るファイル
        │   ├── model/        ← 学習者が触るファイル
        │   └── repository/   ← 学習者が触るファイル
        └── resources/templates/
            └── employee/     ← 学習者が触るファイル
```
