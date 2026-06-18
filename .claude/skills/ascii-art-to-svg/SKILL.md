---
name: ascii-art-to-svg
description: Convert ASCII art diagrams in Markdown files into clean SVG images. Use this skill whenever the user wants to improve, replace, or convert text-based diagrams — including box-drawing characters (┌─┐│└┘), arrow characters (↓→←▼), or numbered-step flows (①②③) inside code blocks. Also triggers when the user says things like "図解をSVGにしたい", "ASCII アートを画像に変換", "ドキュメントの図を見やすくしたい", "replace ASCII art", or "コードブロックの図を画像化". Even if the user just says "docs の図解を改善してほしい", strongly consider using this skill.
---

# ASCII Art → SVG 変換スキル

Markdown ドキュメント内の ASCII アート図解を、きれいな SVG 画像に変換します。

## ステップ 1：ASCII アートを特定する

Markdown の ` ``` ` コードブロック内で以下の文字を含む箇所を探す：

- ボックス罫線: `┌ ┐ └ ┘ │ ─`
- 矢印: `↓ → ← ↑ ▼ ▲`
- 丸数字: `① ② ③ ④ ⑤`

変換が有効な対象 ✅ ：フロー図・シーケンス図・マッピング表  
変換不要な対象 ❌ ：コードサンプル・ファイルツリー・テキスト説明

## ステップ 2：図のタイプを判別する

### A. 縦フロー図

コンポーネントが縦に並ぶ最も一般的なパターン。

```
ブラウザ → BoxA → BoxB → ブラウザ
```

**レイアウト指針：**
- canvas 幅: 480px、中心 x=200
- ボックス: `x=40, width=320, rx=4`
- Browser pill: `width=220, height=40, rx=20`（上下で同じサイズに統一する）
- ボックス間のギャップ: 30〜40px
- 矢印ラベル: ボックス中央より右に14px（`x=214`）

### B. シーケンス図

2つのアクターが横に並び、矢印で往来するパターン。

```
ブラウザ              サーバー
  |──① POST ─────→|
  |←─③ 302 Redirect─|
  |──④ GET ────────→|
```

**レイアウト指針：**
- canvas 幅: 520px
- ブラウザ: `center_x=95`（`x=20, width=150`）
- サーバー: `center_x=435`（`x=350, width=150`）
- ライフライン: `stroke-dasharray="4,4"` の垂直線

### C. マッピングテーブル

左列 → 右列の対応を示すパターン（SQL 列名 → Java フィールド名 など）。

```
LEFT 列         RIGHT 列
────────        ────────
id         →   employee.id
name       →   employee.name
```

**レイアウト指針：**
- canvas 幅: 500px
- 左テーブル: `x=10, width=190`（center_x=105）
- 右テーブル: `x=300, width=190`（center_x=395）
- 矢印: `x=200` → `x=296`
- 行の高さ: `(ボックス高 - ヘッダー高) ÷ 行数` で均等割り（**区切り線の本数 = データ行数**）

### D. 1ボックス詳細図

URL ルーティングなど、1つのコンポーネントと内部処理を示すパターン。

```
ブラウザ（上）
↓ URL
┌─────────────────────┐
│  Controller         │
│  ① 処理1           │
│  ② 処理2           │
└─────────────────────┘
```

**レイアウト指針：**
- canvas 幅: 490px
- ボックス: `x=10, width=470`（左右に余白10px）

## ステップ 3：SVG を作成する

### 必須の定義（すべての SVG に含める）

```xml
<defs>
  <marker id="arr" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
    <polygon points="0 0, 8 3, 0 6" fill="#333"/>
  </marker>
  <marker id="arr-d" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
    <polygon points="0 0, 8 3, 0 6" fill="#999"/>
  </marker>
</defs>
```

- `marker-end="url(#arr)"` — 通常の実線矢印
- `marker-end="url(#arr-d)"` — 破線矢印（データの戻り・簡略フロー）

### フォント規則

```xml
<!-- ボックスタイトル -->
font-family="Arial, sans-serif" font-size="13" font-weight="bold" fill="#333"

<!-- 説明テキスト -->
font-family="Arial, sans-serif" font-size="10" fill="#555"

<!-- 矢印ラベル -->
font-family="Arial, sans-serif" font-size="10" fill="#555"
```

テキストは `text-anchor="middle" dominant-baseline="middle"` を使ってボックス中央に配置する。

### カラーパレット（Spring MVC 教材用）

| コンポーネント | fill | stroke | 用途 |
|---|---|---|---|
| ブラウザ | `#dae8fc` | `#6c8ebf` | 画面・クライアント |
| Controller | `#d5e8d4` | `#82b366` | Spring Controller |
| Repository | `#fff2cc` | `#d6b656` | DB アクセス層 |
| データベース | `#e1d5e7` | `#9673a6` | PostgreSQL 等 |
| View / HTML | `#f8cecc` | `#b85450` | Thymeleaf テンプレート |
| リダイレクト | `#fff2cc` | `#d6b656` | 302 Redirect 等 |
| ハイライト行 | `#ffe6cc` | — | 特筆したいマッピング行 |

他プロジェクトでは用途に合わせて色を変えてよい。これらは draw.io 標準パレット由来。

### canvas 高さの計算

要素を上から積み上げて合計を計算し、最後に **15px の下余白** を加える。

| 要素 | 高さの目安 |
|---|---|
| Browser pill（1行） | 40px |
| Simple box（タイトルのみ） | 50px |
| Box + 1行説明 | 64px |
| Box + 2行説明 | 76px |
| Box + 3行説明 | 88px |
| 矢印ギャップ（ラベルなし） | 30px |
| 矢印ギャップ（ラベルあり） | 40px |

**重要**: `height` 属性と `viewBox` の高さを必ず一致させる。

## ステップ 4：Markdown を更新する

1. SVG を `docs/images/chXX-説明.svg` として保存（例: `ch01-data-flow.svg`）
2. Markdown の対象コードブロックを以下に置換：

```markdown
![図の説明](images/chXX-説明.svg)
```

## チェックリスト（完成後に確認）

- [ ] `height` 属性と `viewBox` の height が一致しているか
- [ ] 最下部の要素が canvas 内に収まっているか（切れていないか）
- [ ] 上下の同種ボックス（Browser pill など）のサイズが統一されているか
- [ ] マッピングテーブルの場合、**区切り線の本数 = データ行数**（n行 → n本の区切り線）になっているか
- [ ] Markdown の画像参照パスが正しいか（`images/` からの相対パス）
