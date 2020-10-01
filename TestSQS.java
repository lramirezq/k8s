package cl.lramirez.sqs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithWebIdentityResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import com.amazonaws.services.sqs.model.SendMessageRequest;

public class TestSQS {
	public static void main(String[] args) throws IOException {
		System.out.println("iniciando program");

		TestSQS sqs = new TestSQS();
		sqs.startSession();

		// Logica para realizar un PUT a una cola de ejemplo
		try {

			System.out.println("Haciendo put...");

			AmazonSQS clienteSQS = AmazonSQSClientBuilder.defaultClient();
			String queueUrl = "https://sqs.us-east-1.amazonaws.com/<numAccount>/lrq";
			SendMessageRequest send_msg_request = new SendMessageRequest().withQueueUrl(queueUrl)
					.withMessageBody("Este es un Mensaje de Prueba").withDelaySeconds(5);
			clienteSQS.sendMessage(send_msg_request);

			System.out.println("PUT OK");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Metodo para utilizar el rol asociado como system account
	 * 
	 * @throws IOException
	 */
	private void startSession() throws IOException {

		// Obtener Valores desde el variables de Ambientes

		// Se obtiene el path del archivo de token desde variable de ambiente de
		// contenedor
		String path = System.getenv("AWS_WEB_IDENTITY_TOKEN_FILE");

		// Se obtiene el ARN del ROL asociado al ServiceAccount del archivo de
		// token desde variable de ambiente de contenedor
		String arnRole = System.getenv("AWS_ROLE_ARN");

		// Se obtiene el separador de line desde el sistema
		String EOL = System.getProperty("line.separator");

		// pasos para obtener el token desde el archivo
		FileReader fr = new FileReader(path);
		BufferedReader br = new BufferedReader(fr);
		String nextLine = "";
		StringBuilder sb = new StringBuilder();
		while ((nextLine = br.readLine()) != null) {
			sb.append(nextLine); // note: BufferedReader strips the EOL
									// character
									// so we add a new one!
			sb.append(EOL);
		}
		// pasamos el token a una variable
		String token = sb.toString();
		System.out.println("Vamos a usar este token : " + token);

		// Logica para obtener credenciales temporales por 3600 segundos = 1 hr.
		// y asumir el rol del service account para la aplicacion.

		try {
			System.out.println("Client Builder");
			AWSSecurityTokenService client = AWSSecurityTokenServiceClientBuilder.standard().build();
			AssumeRoleWithWebIdentityRequest request = new AssumeRoleWithWebIdentityRequest().withRoleArn(arnRole)
					.withRoleSessionName("app1").withWebIdentityToken(token).withDurationSeconds(3600);

			AssumeRoleWithWebIdentityResult response = null;
			try {
				response = client.assumeRoleWithWebIdentity(request);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("Respuesta " + response);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
