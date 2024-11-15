import java.io.*;
import java.net.*;

public class QuizClient {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // IP와 포트 번호 기본값
        int serverPort = 6789;

        try (BufferedReader reader = new BufferedReader(new FileReader("server_info.dat"))) { // server_info.dat에서 서버주소와 포트번호 읽기
            serverAddress = reader.readLine();
            serverPort = Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            System.out.println("Use default server settings: " + serverAddress + ":" + serverPort);
        }

        try (Socket socket = new Socket(serverAddress, serverPort); // 지정된 서버 주소와 포트로 서버에 연결
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 데이터 받기
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // 데이터 보내기
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) { // 입력한 답변을 읽음

            out.println("START"); // 서버에 퀴즈 시작 요청

            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                if (serverMessage.startsWith("QUESTION:")) {
                    System.out.println("Question: " + serverMessage.substring(9)); // 문제 출력
                    System.out.print("Answer: ");
                    String answer = userInput.readLine(); // 답변 입력
                    out.println("ANSWER:" + answer); // 답변 전송
                } else if (serverMessage.startsWith("FEEDBACK:")) { // 피드백 출력
                    System.out.println(serverMessage.substring(9)); // 답 유무 출력
                } else if (serverMessage.startsWith("SCORE:")) {
                    System.out.println("Final Score: " + serverMessage.substring(6)); // 최종 점수 출력
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client Error: " + e.getMessage());
        }
    }
}
