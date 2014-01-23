package ch.systemsx.sybit.crkwebui.server.commons.validators;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import ch.systemsx.sybit.crkwebui.shared.exceptions.ValidationException;

/**
 * Captcha challenge validator.
 * @author AS
 *
 */
public class CaptchaValidator {
	
	private String captchaPublicKey;
	private String captchaPrivateKey;
	
	public CaptchaValidator(String captchaPublicKey,
							String captchaPrivateKey)
	{
		this.captchaPublicKey = captchaPublicKey;
		this.captchaPrivateKey = captchaPrivateKey;
	}
	
	/**
	 * Verifies correctness of the challenge.
	 * @param challenge challenge
	 * @param response response
	 * @param remoteAddress remote address
	 * @throws ValidationException when validation fails
	 */
	public void verifyChallenge(String challenge, 
								String response, 
								String remoteAddress) throws ValidationException
	{
		if((response == null) || (challenge == null))
		{
			throw new ValidationException("Captcha verification not possible");
		}
		else
		{
			ReCaptcha recaptcha = ReCaptchaFactory.newReCaptcha(captchaPublicKey, captchaPrivateKey, true);
			boolean verificationResult = recaptcha.checkAnswer(remoteAddress, challenge, response).isValid();
			
			if(!verificationResult)
			{
				throw new ValidationException("Captcha verification failed - incorrect value provided");
			}
		}
	}
}
