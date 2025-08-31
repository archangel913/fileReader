package filereader;

public class FilerReaderBuilder {
	/** 読み込み対象ファイル */
	private String targetFilePath;
	
	/** ヘッダーを持つかどうか */
	private boolean haveHeader;

	public String getTargetFilePath() {
		return targetFilePath;
	}

	public void setTargetFilePath(String targetFilePath) {
		this.targetFilePath = targetFilePath;
	}
	
	public boolean isHaveHeader() {
		return haveHeader;
	}

	public void setHaveHeader(boolean haveHeader) {
		this.haveHeader = haveHeader;
	}

}
