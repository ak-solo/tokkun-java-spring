# 環境構築手順

この手順では、学習環境のセットアップを行います。
環境構築には **2 つの方法** があります。自分の環境に合った方法を選んでください。

| | 方法 A：Dev Container | 方法 B：ローカル JDK |
|---|---|---|
| 向いている環境 | Mac / Linux | Windows（メモリが少ない PC でも動きやすい） |
| JDK のインストール | 不要（コンテナに含まれる） | 必要 |
| Docker の用途 | アプリ + DB 両方 | DB のみ |

---

## 方法 A：Dev Container を使う

### 必要なもの

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)（Windows / Mac）または Docker Engine（Linux）
- [Visual Studio Code](https://code.visualstudio.com/)
- VS Code 拡張機能：[Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers)

### 手順 1：リポジトリをクローンする

```bash
git clone <リポジトリのURL>
cd tokkun-java-spring
```

### 手順 2：Dev Container を起動する

1. VS Code でリポジトリフォルダを開く
2. 右下に「**Reopen in Container**」の通知が表示されたらクリック
   - 表示されない場合は `F1` キーを押してコマンドパレットを開き、「**Dev Containers: Reopen in Container**」を選択
3. 初回はコンテナのビルドに数分かかります。完了するまで待ってください

### 手順 3：アプリケーションを起動する

VS Code のターミナルで以下を実行します。

```bash
cd src/EmployeeApp
./gradlew bootRun
```

ブラウザで [http://localhost:8080](http://localhost:8080) を開き、社員一覧が表示されれば完了です。

---

## 方法 B：ローカル JDK + Docker（DB のみ）

Windows でメモリ消費を抑えたい場合や、Dev Container の動作が重い場合はこちらを使ってください。

### 必要なもの

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [JDK 21](https://adoptium.net/)（Eclipse Temurin 推奨）
- [Visual Studio Code](https://code.visualstudio.com/)

### 手順 1：リポジトリをクローンする

```bash
git clone <リポジトリのURL>
cd tokkun-java-spring
```

### 手順 2：VS Code 拡張機能をインストールする

VS Code でリポジトリフォルダを開くと、右下に「**推奨拡張機能をインストールしますか？**」の通知が表示されます。「インストール」をクリックしてください。

表示されない場合は、コマンドパレット（`F1`）から「**Extensions: Show Recommended Extensions**」を選択し、一覧に表示された拡張機能をインストールしてください。

### 手順 3：Docker で DB を起動する

プロジェクトのルートフォルダで以下を実行します。

```bash
docker compose up -d
```

PostgreSQL コンテナが起動し、スキーマとサンプルデータが自動で投入されます。

> **停止するとき**
>
> ```bash
> docker compose down
> ```
>
> データを消してリセットしたい場合は `docker compose down -v` を実行してください。

### 手順 4：アプリケーションを起動する

```bash
cd src/EmployeeApp
./gradlew bootRun
```

ブラウザで [http://localhost:8080](http://localhost:8080) を開き、社員一覧が表示されれば完了です。

---

## 共通：VS Code の GUI で起動・デバッグする

ターミナルを使わずに、VS Code のボタンから起動・デバッグ実行することもできます。

### 起動前の確認：Java の準備完了を待つ

初めてコンテナを開いたとき、VS Code は Java プロジェクトを内部で解析・ビルドする処理を自動実行します。この処理が終わるまで、▶ ボタンを押してもエラーになります。

- VS Code 右下のステータスバーに **「Java: Building workspace...」** と表示されている間は待ちます
- 表示が消えたら準備完了です（1〜2 分かかることがあります）

> エラー「Main class ... doesn't exist in the workspace」が出た場合も、これが原因です。
> 待っても解消しない場合は、コマンドパレット（`F1`）から「**Java: Clean Java Language Server Workspace**」を実行してください。

### 起動方法 1：Run and Debug パネル（▶ ボタン）

1. 左サイドバーの「**Run and Debug**」アイコン（または `Ctrl+Shift+D` / `Cmd+Shift+D`）をクリック
2. 上部のドロップダウンが「**Spring Boot**」になっていることを確認
3. ▶ ボタン（または `F5`）をクリック

通常起動（`F5`）でもブレークポイントが有効なデバッグ実行になります。

> **ブレークポイントの使い方**
>
> コードの行番号の左をクリックすると赤い丸（ブレークポイント）が付きます。
> デバッグ実行中にその行に差し掛かると処理が一時停止し、変数の中身などを確認できます。

### 起動方法 2：ソースファイルの CodeLens から実行

1. `src/EmployeeApp/src/main/java/com/example/employeeapp/EmployeeAppApplication.java` を開く
2. `main` メソッドの上に表示される「**Run**」または「**Debug**」のリンクをクリック

---

## 共通：ホットリロードを使う

Spring Boot DevTools を使うと、ファイルを保存するたびにアプリが自動再起動します。  
`bootRun` で起動している場合、ソースを変更してビルドすると自動的に再起動します。

VS Code の場合は「**Java: Build Workspace**」コマンド（`Shift+Alt+B`）でビルドできます。

---

## トラブルシューティング

### `./gradlew bootRun` でエラーが出る

依存関係の解決を試してください。

```bash
./gradlew dependencies
```

### ポート 8080 がすでに使われている

`application.properties` でポートを変更できます。

```properties
server.port=8081
```

### 方法 B で DB に接続できない（`Connection refused`）

Docker Desktop が起動しているか確認してください。起動済みであれば、以下でコンテナの状態を確認します。

```bash
docker compose ps
```

`db` サービスが `running` になっていない場合は、`docker compose up -d` を再実行してください。

### 方法 A と方法 B を同時に起動しない

どちらの方法も PostgreSQL のポート 5432 を使います。両方を同時に起動するとポートが競合してエラーになります。

---

## 次のステップ

環境の準備ができたら [chapter00.md](chapter00.md) から学習を始めましょう。
