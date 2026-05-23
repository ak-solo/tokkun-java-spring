# impl-phase

引数で指定されたフェーズ番号（例: `3`）の実装を行う。

## 手順

### 1. タスク確認

`TODO.md` を読み、指定フェーズの未完了タスクをすべて把握する。

### 2. 参照資料の確認

以下を必ず読んでから実装する：

- `CLAUDE.md` — プロジェクト概要・技術スタック・学習目標
- `.claude/rules/coding-conventions.md` — コーディング規約・JdbcTemplate の使い方
- `.claude/rules/git-commit.md` — コミット方針

**ASP.NET Core 参照先**（フレームワーク違いの同一教材）:
- モデル: `../tokkun-aspnetcore/src/EmployeeApp/Models/`
- コントローラ: `../tokkun-aspnetcore/src/EmployeeApp/Controllers/EmployeeController.cs`
- ビュー: `../tokkun-aspnetcore/src/EmployeeApp/Views/Employee/`
- レイアウト: `../tokkun-aspnetcore/src/EmployeeApp/Views/Shared/_Layout.cshtml`

### 3. フェーズ別の実装内容

フェーズ番号によって作業内容が異なる：

| フェーズ | 作業内容 |
|----------|----------|
| 3 | `docs/chapter00.md` のみ（コードなし） |
| 4 | `docs/chapter01.md` + ch01 解答コード + `ch02-start` タグ |
| 5 | `docs/chapter02.md` + ch02 解答コード + `ch03-start` タグ |
| 6 | `docs/chapter03.md` + ch03 解答コード + `ch04-start` タグ |
| 7 | `docs/chapter04.md` + ch04 解答コード + `ch05-start` タグ |
| 8 | `docs/chapter05.md` + ch05 解答コード + `ch06-start` タグ |
| 9 | `docs/chapter06.md` + ch06 解答コード + `ch07-start` タグ |
| 10 | `docs/chapter07.md` のみ（仕様書形式） |
| 11 | `docs/chapter08.md` のみ（仕様書形式） |

### 4. ドキュメント（docs/chapterXX.md）の書き方

**読者**: tokkun-java ch01–10 と tokkun-sql ch00–11 を修了した初学者。

**ドキュメント構成**（コードを伴うフェーズ）:

```
## 基礎知識
- この章で学ぶ概念を図や箇条書きで説明
- Spring MVC 固有の概念はコード例を交えて説明
- 「tokkun-sql で書いた〇〇と同じ」など既習知識に繋げる

## スターターコードの説明
- 学習者が受け取った時点で何が動いていて何が動いていないかを説明

## 練習問題（4〜6 問）
- 問1（簡単）: コードの一部を埋めるだけ
- 問2〜4（普通）: 1〜3 行の変更で解ける
- 問5〜6（応用）: 複数ファイルをまたぐ変更
- 各問題に「ヒント」セクションを付ける
- 解答例は載せない（別ブランチに存在する）
```

**docs/chapter07.md・docs/chapter08.md**（仕様書形式）:

```
## 作成する画面の仕様
- URL 設計・表示項目・ソート条件・バリデーション条件を箇条書きで明示
- スターターコードは提供しない
- ヒントは「詰まったときに読む」程度に最小化
```

### 5. コードの実装方針

**変更対象ファイル**（学習者が触る箇所のみ変更する）:
- `src/EmployeeApp/src/main/java/com/example/employeeapp/controller/EmployeeController.java`
- `src/EmployeeApp/src/main/java/com/example/employeeapp/model/` 配下
- `src/EmployeeApp/src/main/resources/templates/employee/` 配下

**新規追加が必要な場合**（ch07–08 の repository 追加など）:
- `src/EmployeeApp/src/main/java/com/example/employeeapp/repository/` 配下

**スターター状態からの差分として実装する**:
- 前フェーズのタグ（`chXX-start`）が「学習者が受け取る状態」
- 今フェーズの解答コードが「次フェーズのスターター状態」になる

**JdbcTemplate の使い方（必ず従うこと）**:
- `NamedParameterJdbcTemplate` を使い `:paramName` 形式のプレースホルダを使う
- コントローラから直接 JdbcTemplate を呼ばず Repository に委譲する
- `BeanPropertyRowMapper<>(Employee.class)` でマッピングする

### 6. 動作確認（コードを変更したフェーズのみ）

Dev Container の app コンテナ（`devcontainer-app-1`）を使って確認する。

```bash
# コンテナが起動していない場合
docker compose -f .devcontainer/docker-compose.yml up -d --build app

# ビルド
docker exec devcontainer-app-1 bash -c "cd /workspaces/tokkun-java-spring/src/EmployeeApp && ./gradlew build --no-daemon"

# アプリ起動（バックグラウンド）
docker exec -d devcontainer-app-1 bash -c "
  cd /workspaces/tokkun-java-spring/src/EmployeeApp &&
  SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/employeeapp
  SPRING_DATASOURCE_USERNAME=postgres
  SPRING_DATASOURCE_PASSWORD=postgres
  ./gradlew bootRun --no-daemon > /tmp/bootrun.log 2>&1
"

# 起動待ち（curl で確認）
# 該当画面が正常に表示されるまでポーリング
```

**確認観点**:
- 新しく追加した画面・機能が表示されること
- 既存画面（社員一覧など）が壊れていないこと
- ログにエラーが出ていないこと（`docker exec devcontainer-app-1 tail /tmp/bootrun.log`）

### 7. コミット

`git-commit.md` の方針に従い目的ごとに分割する：

1. `docs:` ドキュメント追加
2. `feat:` 解答コード（controller / repository / model の変更）
3. `feat:` テンプレート追加・変更
4. `docs:` TODO.md チェック更新

タグが必要なフェーズ（4〜9）は最後のコミット後に `git tag chXX-start` を付ける。

### 8. TODO.md 更新

完了したタスクにチェック `[x]` を付けてコミットする。
