package filereader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ファイルを読み込み、java内で使用できるようにするライブラリ
 */
public class CustomizedFileReader {

	/** 読み込み対象ファイル */
	private File targetFile;

	/** ヘッダーを持つかどうか */
	private boolean haveHeader;

	/**
	 * ユーザーから直接インスタンス化させない
	 */
	private CustomizedFileReader() {
	}

	public List<Map<String, String>> read() {
		// 読み込み可能かどうか判定する
		if (canRead()) {
			return null;
		}

		List<Map<String, String>> result = new ArrayList<>();

		// 読み込み可能であった場合、BufferedReaderを生成
		try (BufferedReader br = new BufferedReader(new FileReader(targetFile))) {
			boolean isHeader = haveHeader;
			String[] headers = {};
			String[] values = {};

			while (true) {
				values = readLine(br);

				if (values.length == 0) {
					break;
				}

				if (isHeader) {
					// ヘッダーを持つ場合、ヘッダーを読み込む
					headers = values.clone();
					isHeader = false;
				} else {
					// データ本体を取得
					Map<String, String> mappedValues = new HashMap<>();
					if (headers.length == values.length) {
						for (int i = 0; i < headers.length; ++i) {
							mappedValues.put(headers[i], values[i]);
						}
					} else if (headers.length == 0) {
						// ヘッダーが設定されていなければ
						for (int i = 0; i < values.length; ++i) {
							mappedValues.put(String.valueOf(i), values[i]);
						}
					} else {
						// ヘッダーとデータの個数が違う
						throw new Exception("ヘッダーとデータの個数が違います");
					}
					result.add(mappedValues);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * 
	 * @param line
	 * @return
	 * @throws Exception 
	 */
	private String[] readLine(BufferedReader br) throws Exception {
		String line = br.readLine();

		if (Objects.isNull(line)) {
			// 次の行が読めなかった場合、長さ0の配列を返す
			return new String[0];
		}

		List<String> results = new ArrayList<>();

		// カンマ区切りで分割
		String[] values = line.split(",");
		for (int i = 0; i < values.length; ++i) {
			StringBuffer sb = new StringBuffer();
			// ダブルクォートが含まる場合
			if (values[i].contains("\"")) {

				while (true) {
					if (values[i].startsWith("\"") && values[i].endsWith("\"")) {
						// 先頭にも後方にもダブルクォートがある場合

						// 先頭のダブルクォート除去
						values[i] = values[i].substring(1);
						// 最後尾のダブルクォート除去
						values[i] = values[i].substring(0, values[i].length() - 1);
						// エスケープされたダブルクォートを置換
						values[i] = values[i].replace("\"\"", "\"");

						// バッファへ追加
						sb.append(values[i]);
						++i;
						// ループを抜ける
						break;
					} else if (values[i].startsWith("\"")) {
						// 先頭にダブルクォートがある場合

						// 先頭のダブルクォート除去
						values[i] = values[i].substring(1);
						// エスケープされたダブルクォートを置換
						values[i] = values[i].replace("\"\"", "\"");
						// バッファへ追加
						sb.append(values[i]);
						//splitでカンマが消えているので、追加する
						sb.append(",");

						++i;
					} else if (values[i].endsWith("\"")) {
						// 最後尾にダブルクォートがある場合

						// 最後尾のダブルクォート除去
						values[i] = values[i].substring(0, values[i].length() - 1);
						// エスケープされたダブルクォートを置換
						values[i] = values[i].replace("\"\"", "\"");
						// バッファへ追加
						sb.append(values[i]);

						++i;
						// ループを抜ける
						break;
					} else if (values[i].contains("\n")
							|| values[i].contains("\r\n")
							|| values[i].contains("\r")) {
						// 改行が含まれている場合

						// エスケープされたダブルクォートを置換
						values[i] = values[i].replace("\"\"", "\"");
						// バッファへ追加
						sb.append(values[i]);

						// ダブルクォートで囲まれている中に改行が含まれているので、
						// 次の行を読み込み、続きを読む
						line = br.readLine();

						if (Objects.isNull(line)) {
							// データが壊れているので例外を投げる
							throw new Exception("ダブルクォートが閉じられていません");
						}

						// カンマ区切りで分割
						// 今読んでいる変数を更新
						values = line.split(",");
						i = 0;
					} else {
						// ダブルクオートの間で、さらにカンマに囲まれていた場合

						// エスケープされたダブルクォートを置換
						values[i] = values[i].replace("\"\"", "\"");
						// バッファへ追加
						sb.append(values[i]);
						//splitでカンマが消えているので、追加する
						sb.append(",");

						++i;
					}
				}
			} else {
				// ただの文字列の場合

				// エスケープされたダブルクォートを置換
				values[i] = values[i].replace("\"\"", "\"");
				// バッファへ追加
				sb.append(values[i]);
			}

			// 取得した文字列を格納する
			results.add(sb.toString());
		}
		return results.toArray(new String[results.size()]);
	}

	private boolean canRead() {
		// ファイルが存在しなければfalse
		if (!targetFile.exists()) {
			System.err.println("ファイルが存在しません");
			return false;
		}

		// ファイルで無ければfalse
		if (!targetFile.isFile()) {
			System.err.println("ファイルではありません");
			return false;
		}

		// 読み込み可能でなければfalse
		if (!targetFile.canRead()) {
			System.err.println("読み込むことができません");
			System.err.println("読み込み権限があること・他プロセスに占有されていないかの確認をしてください");
			return false;
		}

		// 問題なく読み込める状況ならばtrue
		return true;
	}

	/**
	 * FileReaderのビルダーを返す
	 * @return
	 */
	public static FilerReaderBuilder getBuilder() {
		return new FilerReaderBuilder();
	}

	/**
	 * ビルダーをもとにFileReaderのインスタンスを返す
	 * @param builder
	 * @return
	 */
	public static CustomizedFileReader build(FilerReaderBuilder builder) {
		CustomizedFileReader reader = new CustomizedFileReader();
		reader.targetFile = new File(builder.getTargetFilePath());
		reader.haveHeader = builder.isHaveHeader();

		return reader;
	}
}
