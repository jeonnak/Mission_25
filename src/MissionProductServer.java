// 서버 클래스와 소켓 클라이언트 중첩 클래스 선언

//  처리결과를 제이슨.. 클라이언트로 응답 다시 보내기

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.json.JSONArray;
import org.json.JSONObject;

public class MissionProductServer {
    //필드
    private ServerSocket serverSocket;
    private ExecutorService threadPool;

    // 서버는 상품을 List<MissionProduct> 메모리에 저장하여 관리합니다.
    private List<MissionProduct> products;
    private int sequence;


    //메소드: 서버 시작
    public void start() throws IOException {
        serverSocket = new ServerSocket(50001);
        threadPool = Executors.newFixedThreadPool(100);
        products = new Vector<MissionProduct>();

        System.out.println( "[서버] 시작됨");

        while(true) {
            //연결 수락
            Socket socket = serverSocket.accept();
            //요청 처리용 SocketClient 생성
            SocketClient sc = new SocketClient(socket);
        }
    }
    //메소드: 서버 종료
    public void stop() {
        try {
            serverSocket.close();
            threadPool.shutdownNow();
            System.out.println( "[서버] 종료됨 ");
        } catch (IOException e1) {}
    }
    //중첩 클래스: 요청 처리
    public class SocketClient {
        //필드
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;

        //생성자
        public SocketClient(Socket socket) {
            try {
                this.socket = socket;
                this.dis = new DataInputStream(socket.getInputStream());
                this.dos = new DataOutputStream(socket.getOutputStream());
                receive();
            } catch(IOException e) {
                close();
            }
        }

        //메소드: 요청 받기
        public void receive() {
            threadPool.execute(() -> {
                try {
                    while(true) {
                        String receiveJson = dis.readUTF();

                        JSONObject request = new JSONObject(receiveJson);
                        int menu = request.getInt("menu");

                        switch(menu) {
                            case 0 -> list(request);
                            case 1 -> create(request);
                            case 2 -> update(request);
                            case 3 -> delete(request);
                        }
                    }
                } catch(IOException e) {
                    close();
                }
            });
        }

        public void list(JSONObject request) throws IOException {
            JSONArray data = new JSONArray();
            for(Product p : products) {
                JSONObject product = new JSONObject();
                product.put("no", p.getNo());
                product.put("name", p.getName());
                product.put("price", p.getPrice());
                product.put("stock", p.getStock());
                data.put(product);
            }

            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", data);
            dos.writeUTF(response.toString());
            dos.flush();
        }

        public void create(JSONObject request) throws IOException {
            JSONObject data = request.getJSONObject("data");
            MissionProduct product = new MissionProduct();
            product.setNo(++sequence);
            product.setName(data.getString("name"));
            product.setPrice(data.getInt("price"));
            product.setStock(data.getInt("stock"));
            products.add(product);

            //응답 보내기
            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", new JSONObject());
            dos.writeUTF(response.toString());
            dos.flush();
        }

        public void update(JSONObject request) throws IOException {
            //요청 처리하기
            JSONObject data = request.getJSONObject("data");
            int no = data.getInt("no");
            for(int i= 0; i<products.size(); i++) {
                MissionProduct product = products.get(i);
                if(product.getNo() = = no) {
                    product.setName(data.getString("name"));
                    product.setPrice(data.getInt("price"));
                    product.setStock(data.getInt("stock"));
                }
            }

            //응답 보내기
            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", new JSONObject());
            dos.writeUTF(response.toString());
            dos.flush();
        }

        public void delete(JSONObject request) throws IOException {
            //요청 처리하기
            JSONObject data = request.getJSONObject("data");
            int no = data.getInt("no");
            Iterator<MissionProduct> iterator = products.iterator();
            while(iterator.hasNext()) {
                MissionProduct product = iterator.next();
                if(product.getNo() = = no) {
                    iterator.remove();
                }
            }

            //응답 보내기

            JSONObject response = new JSONObject();
            response.put("status", "success");  // status : 처리 상태
            response.put("data", new JSONObject());  // data : 클라이언트로 전달하려는 데이터 넣기
            dos.writeUTF(response.toString());
            dos.flush();
        }

        //메소드: 연결 종료
        public void close() {
            try {
                socket.close();
            } catch(Exception e) {}
        }
    }
    public static void main(String[] args) {
        MissionProductServer productServer = new MissionProductServer();
        try {
            productServer.start();
        } catch(IOException e) {
            System.out.println(e.getMessage());
            productServer.stop();
        }
    }
}
