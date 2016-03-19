# tabijimanOSS-Android / 旅自慢OSS-Android

This app is released as a sample app with opendata (provided from SPARQL endpoint)


## 目次

- [このアプリについて](#このアプリについて)
    * [概要](#概要)
    * [使い方](#使い方)
    * [開発環境](#開発環境)
    * [検証済み環境](#検証済み環境)
    * [ライセンス](#ライセンス)
- [カスタマイズ](#カスタマイズ)
    * [パッケージ名を変更する](#パッケージ名を変更する)
    * [スプラッシュ画像を変更する](#スプラッシュ画像を変更する)
    * [アイコン画像を変更する](#アイコン画像を変更する)
    * [同梱カオハメフレームを変更する](#同梱カオハメフレームを変更する)
    * [とりあえず動作を変更する](#とりあえず動作を変更する)
- [開発情報](#開発情報)
    * [Keystore　の設定　と　GoogleMapsAPIキー の設定](#Keystore　の設定　と　GoogleMapsAPIキー の設定)
    * [ライブラリ](#ライブラリ)
- [オープンデータ](#オープンデータ)
    * [endpoint](#endpoint)
    * [データ提供](#データ提供)
- [謝辞](#謝辞)

## このアプリについて


### 概要

旅自慢OSS は、 観光マップ＆カオハメフレーム合成アプリです。オープンデータ活用実証のために、開発しました。
福井システム工業会が開発を行ったので、初期ロジックは福井をメインで扱う内容となっています。

- 観光マップ：
    * SPARQLエンドポイントから取得した観光情報を利用
    * 全国の観光情報を地図・詳細画面に表示
- カオハメフレーム：
    * SPARQLエンドポイントから取得したカオハメフレーム情報を利用 （今後、増殖予定！）
    * ２種類の内蔵フレームが同梱 （福井県福井市、福井県鯖江市）
    * カオハメフレームで写真をとり、Twitter, Facebook へシェアできます
- 多言語： ※部分的対応
    * 日・英の対応したリソースを同梱　（英語はリソース同梱のみ)
    * (Android版では、中国語・韓国語・ポルトガル語対応の部分対応はありません)


### 使い方

Android Studio のプロジェクトになっています。

- git clone でローカルにソースコードを展開して下さい。
- 必要なカスタマイズなどをおこなってください


### 開発環境

- Android SDK & Android Studio 1.5.1 [こちらからダウンロードできます](http://developer.android.com/intl/ja/sdk/index.html)
- JDK [こちらからダウンロードできます](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Google Play servicesパッケージ
    * Android Studio > SDK Manager　> [SDK Tool]タブ からダウンロードが可能
    * Google Map利用のために入れます

### 検証済み環境

- Galaxy Note (SCL22) + Android 5.0 (Lollipop / API level 21)
- HUAWEI (G620S−L02) + Android 4.4.4 (KitKat / API level 19)


### ライセンス

- 旅自慢OSS ソフトウェア部分 は MITライセンス の元で配布されます。 LICENSE.txtを、参照ください。
- 旅自慢OSS 同梱のアイコン・画像は [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/deed.ja) の元で配布されます。


## カスタマイズ

アプリをカスタマイズする基本的な箇所の説明です。


### パッケージ名を変更する

1. パッケージを開く
2. main/res を開き 日本語表記は values-ja 英語表記は values を開きます
3. その中の strings.xml の中を修正します

```java
// アプリ名の変更
<string name="app_name">TabijimanOSS</string>
```


### スプラッシュ画像を変更する

スプラッシュ画像は res/drawable/launchimage.png に置いてあります。  
端末に応じて大きさが変更されます。
そのためスプラッシュ画面に白の余白が生まれることがありますが  
画像の背景と同じ色を res/layout/splash.xml の中にある imageView の background に設定してください。  
またファイル名を変更する場合は res/layout/splash.xml の記述を変更してください。  

※旅自慢OSSでは、launchimage.png の背景色は #00b5e2 の単色にしてあり  
ImageViewのbackgroundも #00b5e2 に設定してあります


### アイコン画像を変更する

アイコン画像は res/mipmap-〇〇/ic_launcher.png に置いてあります。  
〇〇は解像度によって異なります。必要であれば、解像度にあわせた画像を配置してください。  
1024 × 1024 のicon画像などがあれば [MakeAppIcon](http://makeappicon.com) から自動で生成することができます  
ファイル名を変更する場合は AndroidManifest.xml の application ダグ内にある android:icon を変更してください


### 同梱カオハメフレームを変更する

タイトルなどの文字情報　と　画像ファイル の２つにわけて管理されています。

- 文字情報：
    * main/assets/initFrameJSON.json で ja/en の言語別に管理しています
    * 各item > img にかいたファイル名により、該当画像を指定します
- 画像ファイル：
    * main/assets の中に　フレーム画像 が配置されています
    * 変更や追加を行う場合には、Android Studio の 該当箇所に Finder からドラッグ＆ドロップしてください
    * カオハメしたい箇所を透過処理したPNGファイルを配置します
    * おすすめのサイズは W1080xH1440 です


### とりあえず動作を変更する

設定パラメーター系は　main/java/net.e_fas.oss.tabijiman/AppSetting.javaに、可能な限り集約してありますので、まずこのファイルをみてもらうのが良いと思います。
いくつか抜粋して紹介しますので、変更して動作させてみてください。

コード中からは、 *AppSetting.変数名* のようにして呼び出しています。　

```
...
    public static String shereTag = "#tabijiman"
...
    // SPARQL
    // 観光情報を取得
    public static String query_place =
            "select ?s ?name ?cat ?lat ?lng ?add ?name ?desc ?img {"
            + "?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/jrrk#CivicPOI>;"
            + "<http://imi.ipa.go.jp/ns/core/rdf#種別> ?cat;"
            + "<http://www.w3.org/2003/01/geo/wgs84_pos#long> ?lng;"
            + "<http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat;"
            + "<http://www.w3.org/2000/01/rdf-schema#label> ?name;"
            + "<http://imi.ipa.go.jp/ns/core/rdf#説明> ?desc;"
            + "<http://schema.org/image> ?img; }";
...
```

## 開発情報

### Keystore　の設定　と　GoogleMapsAPIキー の設定

KeystoreとAPIキー取得：

- 次の記事などを参照して、APIキー取得の準備と取得を行います
    * [Google Maps Android API覚え書き](http://m-kawato.hatenablog.com/entry/2015/05/11/222852)
    * ※debug.keystore　を仮で用意してあります※


APIキーの書き込みファイル：

1. 用意したKeystoreのSHA値で取得した、APIキーを準備
2. main/res を開き google_maps_api.xml (debug) を開きます
3. XMLファイル中の google_maps_key の値を修正します


※Google Play 公開時のreleaseビルド時には、別途release用の手順が必要です

- release用 APIキー 記載ファイル
    - app/src/release/res/values/google_maps_api.xml
    - Project Filesビューか、Finder/Explorerで表示させる　（Androidビューでは表示されない為）

### ライブラリ

各種ライブラリの利用用途

- blurry（jp.wasabeef:blurry:1.1.0）
    * フレームエフェクトに利用


## オープンデータ

### endpoint

本アプリでは [odp](http://odp.jig.jp/) 提供の　endpointにアクセスして、全国の自治体が公開している観光情報オープンデータを参照しています。

- [odp 開発者サイト](http://developer.odp.jig.jp/)  ※利用情報などがあります
- [odp SPARQLコンソール](http://sparql.odp.jig.jp/sparql.html)  ※ブラウザで表示する　チェックをいれて試行錯誤するのがオススメ

### データ提供

[odp データ提供](http://developer.odp.jig.jp/data/#assetlist) にある提供一覧の中より、<http://purl.org/jrrk#CivicPOI> に関連付けられてる、かつ画像を持つものなどの条件のもと取得して利用しています。

公開時にマップ上にできている自治体提供情報は、次になるようです。

- 北海道室蘭市
- 東京都品川区
- 福井県、県内各自治体
- 静岡県島田市
- 兵庫県神戸市
- 広島県尾道市


## 謝辞

次の皆様の協力のもと、本アプリを完成させることが出来ました。末筆ではありますが、謝辞を述べさせていただきます。

- 各種ライブラリを提供いただいている開発者・開発会社様
- イラストを無料提供いただいてる　[いらすとや](http://www.irasutoya.com/)　様
- 地元オープンデータを提供されてる自治体・担当者様
- odp提供会社の皆様
- 福井県システム工業会の関係者の皆様
- プロジェクトメンバーのプログラマー＆デザイナーの方々
