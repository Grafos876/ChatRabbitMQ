package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Chat {
  
  	static LocalDateTime tempo_atual = LocalDateTime.now();
  	final static DateTimeFormatter data = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    static String data_formatada = "";
    final static DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm");
    static String hora_formatada = "";
    static String QUEUE_DESTINO = "";
    
  public static void main(String[] argv) throws Exception {
	  	
	 	ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("34.234.94.37"); // Alterar
	    factory.setUsername("sd_job"); // Alterar
	    factory.setPassword("1234"); // Alterar
	    factory.setVirtualHost("/");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    
	    //Entrada
	    int indicador_conexao = 0;
	  	Scanner entrada = new Scanner(System.in);
	  	String mensagem = "";
	    String mensagem_exibicao = "User: ";
	    
	    System.out.printf(mensagem_exibicao);
	    mensagem = entrada.nextLine();
	    
	    //Filas
	    String QUEUE_NAME = mensagem;
    	
	    mensagem_exibicao = ">> ";
	    
    	while(indicador_conexao == 0) {
	    	System.out.printf(mensagem_exibicao);
	    	mensagem = entrada.nextLine();
		    if(mensagem.charAt(0) == '@') {
		    	QUEUE_DESTINO = mensagem.substring(1);
		    	mensagem_exibicao = mensagem+">>";
		    	indicador_conexao ++;
		    }else {
		    	System.out.println("Sem conexão estabelecidade com outro usuário.");
		    }
	    }
    	
    	channel.queueDeclare(QUEUE_NAME, false,   false,     false,       null); //A fila do usuário atual
    	channel.queueDeclare(QUEUE_DESTINO, false,   false,     false,       null); // A fila do usuário no qual deseja estabelecer conexão
        
    	Consumer consumer = new DefaultConsumer(channel) {
    	      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)           throws IOException {
    	    	tempo_atual = LocalDateTime.now();
    	    	data_formatada = data.format(tempo_atual);
    	    	hora_formatada = hora.format(tempo_atual);
    	    	String message = new String(body, "UTF-8");
    	    	
    	    	String completa = "\n("+data_formatada+" às "+hora_formatada+")"+message;
    	    	
            System.out.println(completa);
    	     
            String newMes = "@"+QUEUE_DESTINO+">> ";
            System.out.printf("%s", newMes);
  	      }
  	    };
  	    
  	    //(queue-name, autoAck, consumer);    
	    channel.basicConsume(QUEUE_NAME, true,    consumer);
  	    
    	while(true) {
            System.out.printf("%s", mensagem_exibicao);
            mensagem = entrada.nextLine();
          
            if(mensagem.charAt(0) == '@') {
    		    	QUEUE_DESTINO = mensagem.substring(1);
    		    	mensagem_exibicao = mensagem+">>";
    		    	indicador_conexao ++;
            }else {
            	mensagem = " "+QUEUE_NAME+" diz: "+mensagem;
            	channel.basicPublish("",       QUEUE_DESTINO, null,  mensagem.getBytes("UTF-8"));
            }
    	}
    
  }
}