package ch.systemsx.sybit.crkwebui.server.files.uploader.data;

import java.io.Serializable;

import org.apache.commons.fileupload.FileItem;

public class UploadingData implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final FileItem fileToUpload;
	private final String captchaResponse;
	private final String captchaChallenge;
	
	public UploadingData(FileItem fileToUpload,
						 String captchaResponse,
						 String captchaChallenge)
	{
		this.fileToUpload = fileToUpload;
		this.captchaResponse = captchaResponse;
		this.captchaChallenge = captchaChallenge;
	}
	
	public FileItem getFileToUpload() {
		return fileToUpload;
	}

	public String getCaptchaResponse() {
		return captchaResponse;
	}

	public String getCaptchaChallenge() {
		return captchaChallenge;
	}	
}
