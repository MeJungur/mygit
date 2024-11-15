import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class QuizServer {
    private static String serverAddress = "localhost"; //IP와 포트 번호의 기본값
    private static int serverPort = 6789; 

    private static final String[][] QUESTIONS = { //문제와 답
            {"What device blocks unauthorized access and controls network traffic?", "Firewall"}, 
            {"What technology translates internal IPs to a single public IP?", "NAT"}, 
            {"What combines an IP and port to enable network communication?", "Socket"} 
        };

    public static void main(String[] args) {
        // server_info.dat 파일에서 서버 정보 읽기
        try (BufferedReader reader = new BufferedReader(new FileReader("server_info.dat"))) {
            serverAddress = reader.readLine();  //IP 주소
            serverPort = Integer.parseInt(reader.readLine());  //포트 번호
        } catch (IOException e) {
            System.out.println("Use default server settings: " + serverAddress + ":" + serverPort);
        }

        // Server Socket 생성
        ExecutorService threadPool = Executors.newFixedThreadPool(5); // 최대 5개의 클라이언트를 동시에 처리 (멀티클라이언트)

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("QuizServer is running on port" + serverPort + ".");

            while (true) { 
                Socket clientSocket = serverSocket.accept(); //클라이언트 연결 기다리기
                System.out.println("A new client has been connected.");
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("Server Error: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    //각 클라이언트의 연결을 처리
    private static class ClientHandler implements Runnable { 
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket; //클라이언트와 데이터를 주고받음
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); //클라이언트로부터 입력받기
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) { //클라이언트에게 보내기

                String clientMessage = in.readLine(); // 클라이언트 명령 읽기
                int score = 0;

                if (clientMessage != null && clientMessage.equals("START")) {
                    for (String[] questionAnswer : QUESTIONS) {
                        out.println("QUESTION:" + questionAnswer[0]); //QUESTIONS 배열을 순차적으로 읽으면서 클라이언트에게 질문 전송
                        String answer = in.readLine(); // 클라이언트 답변 대기
                        if (answer != null && answer.startsWith("ANSWER:")) {
                            answer = answer.substring(7); //새로 답을 받기 위해 ANSWER: 제거
                            if (answer.equalsIgnoreCase(questionAnswer[1])) {
                                score++;
                                out.println("FEEDBACK:Correct"); //피드백을하여 Correct 또는 Incorrect 전송
                            } else {
                                out.println("FEEDBACK:Incorrect:" + questionAnswer[1]);
                            }
                        }
                    }
                    out.println("SCORE:" + score + "/" + QUESTIONS.length); // 최종 점수 전송
                }

            } catch (IOException e) {
                System.out.println("Error processing client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close(); //작업이 끝난 후 리소스 해제
                } catch (IOException e) {
                    System.out.println("Socket closing error: " + e.getMessage());
                }
            }
        }
    }
}