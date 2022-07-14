import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;

public class Server2{

    DataInputStream dis=null;
    FileOutputStream fos=null;
    BufferedOutputStream bos=null;
    ObjectOutputStream outstream;
    BufferedInputStream bis=null;
    public static void main(String[] args){
        Server2 ser=new Server2();
        try{
            ser.connect();
        }catch(Exception e) {
            System.out.println("오류");
        }
        
    }

    public void connect(){
        int portN=8910;
        ServerSocket serverSocket;
        try{
            System.out.println("서버 시작");
            serverSocket=new ServerSocket(portN);
            System.out.println("대기중..");
            
            while(true){
                Socket socket = serverSocket.accept(); //클라이언트가 접근했을 때 accept() 메소드를 통해 클라이언트 소켓 객체 참조
                InetAddress clientHost = socket.getLocalAddress();
                int clientPort = socket.getPort();
                System.out.println("클라이언트 연결됨. 호스트 : " + clientHost + ", 포트 : " + clientPort);



                // ObjectInputStream instream = new ObjectInputStream(socket.getInputStream()); //소켓의 입력 스트림 객체 참조
                // Object obj = instream.readObject(); // 입력 스트림으로부터 Object 객체 가져오기
                // System.out.println("클라이언트로부터 받은 데이터 : " + obj); // 가져온 객체 출력

                //사진 받기 

                receive(socket);
                
                
                //결과 보내기
                /* 
                BufferedReader re=new BufferedReader(new FileReader("D:/e/새 폴더/result.txt"));
                String str;          
                while((str=re.readLine())!=null){
                    System.out.println(str);
                    outstream = new ObjectOutputStream(socket.getOutputStream()); //소켓의 출력 스트림 객체 참조
                    outstream.writeObject(str); //출력 스트림에 응답 넣기
                }
                outstream.flush(); // 출력
                outstream.close();
                re.close();
                */
                
                socket.close(); //소켓 해제
            }
        }catch(Exception e){
            System.out.println("while문 오류");
        }
    }


    public void receive(Socket socket){
        String filePath="fromandroid.jpg";

        try{
            /* //방법1
            dis=new DataInputStream(socket.getInputStream());
            System.out.println("파일 수신 작업을 시작합니다.");
            //파일 생성하고 파일에 대한 출력 스트림 생성
            File file=new File(filePath);
            System.out.println("파일 존재함? "+file.isFile());
            fos=new FileOutputStream(file);
            //bos=new BufferedOutputStream(fos);
            System.out.println("fromandroid.jpg 파일 생성하였습니다");

            //바이트 데이터를 전송받으면서 기록
            int len;
            int size=4096;
            byte[] data=new byte[size];
            System.out.println("파일에 적기");
            while((len=dis.read(data))!=-1){
                //bos.write(data,0,len);
                fos.write(data,0,len);
            }
            //bos.flush();
            fos.flush();
            System.out.println("받은 파일의 크기 "+file.length());
            */

            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            File f=new File(filePath);
            FileOutputStream output=new FileOutputStream(f);
            byte[] buf=new byte[1024];
            int readBytes;
            while((readBytes=socket.getInputStream().read(buf))!=-1){
                output.write(buf,0,readBytes);
            }
            System.out.println("받은 파일의 크기 "+f.length());
            in.close();
            output.close();
            
            System.out.println("수완");
            
        
        }catch(IOException e){
            System.out.println("IOExceptio 오류");
        }
        /* 
        finally{
            try{
                //bos.close();
                fos.close();
                dis.close();
                System.out.println("스트림 닫음");
                
            }
            catch(IOException e){
                System.out.println("IOException close 오류");
            }     
            
        }
        */
    }
}

