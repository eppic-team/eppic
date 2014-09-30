package ch.systemsx.sybit.crkwebui.server.files.uploader.generators;

import java.util.List;

import org.apache.commons.fileupload.FileItem;

import ch.systemsx.sybit.crkwebui.server.files.uploader.data.UploadingData;

public class UploadingDataGenerator
{
	public static UploadingData generateUploadingData(List<FileItem> items)
	{
		String captchaResponse = null;
		String captchaChallenge = null;
		
		FileItem fileToUpload = null;
		
		for (FileItem item : items) 
		{
			if(item.isFormField())
			{
				if(item.getFieldName() != null)
				{
					if(item.getFieldName().equals("recaptcha_response_field"))
					{
						captchaResponse = item.getString();
					}
					else if(item.getFieldName().equals("recaptcha_challenge_field"))
					{
						captchaChallenge = item.getString();
					}
				}
			}
			else 
			{
				fileToUpload = item;
			}
		}
		
		return new UploadingData(fileToUpload, 
								 captchaResponse, 
								 captchaChallenge);
	}
}
