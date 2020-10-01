package cl.lramirez.secrets;

import java.nio.ByteBuffer;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.*;
import com.amazonaws.services.secretsmanager.model.*;

public class GetSecrets {
	public static void main(String[] args) {
		getSecret();
	}

	/**
	 * Metodo para obtener un Secrets desde AWS Secrets Manager
	 */
	public static void getSecret() {

		// Nombre de Secrets a Obtener
		String secretName = "nameSecrets";

		// Endpoint de Secrets Manager
		String endpoint = "secretsmanager.us-east-1.amazonaws.com";

		// Region AWS a utilizar
		String region = "us-east-1";

		// Logica para obtener el secrets en AWS
		AwsClientBuilder.EndpointConfiguration config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
		AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
		clientBuilder.setEndpointConfiguration(config);
		AWSSecretsManager client = clientBuilder.build();

		String secret;
		ByteBuffer binarySecretData;
		GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName)
				.withVersionStage("AWSCURRENT");
		GetSecretValueResult getSecretValueResult = null;
		try {
			getSecretValueResult = client.getSecretValue(getSecretValueRequest);

		} catch (ResourceNotFoundException e) {
			System.out.println("The requested secret " + secretName + " was not found");
		} catch (InvalidRequestException e) {
			System.out.println("The request was invalid due to: " + e.getMessage());
		} catch (InvalidParameterException e) {
			System.out.println("The request had invalid params: " + e.getMessage());
		}

		if (getSecretValueResult == null) {
			return;
		}

		// Depending on whether the secret was a string or binary, one of these
		// fields will be populated
		if (getSecretValueResult.getSecretString() != null) {
			secret = getSecretValueResult.getSecretString();
			System.out.println(secret);
		} else {
			binarySecretData = getSecretValueResult.getSecretBinary();
			System.out.println(binarySecretData.toString());
		}

	}

}
