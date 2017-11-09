
public class Bus {
	IntegrationClientAPI api = new IntegrationClientAPI("D:\\esbclient.properties");
    
    // 웹 서비스 활용자 인증처리를 한다.
    System.out.println("---------------------------------------------");
    api.auth(UserType.USER);

    // 웹 서비스 활용자를 위한 서비스 인증키가 생성된다. 
    String madesskey = api.makeMobileKey("A12345", "test");

    // 웹 서비스 활용자를 위한 서비스 인증키(session key)를 발행한 후 인증 서버에 등록한다.
    api.sendSessionKey(madesskey);
    
    System.out.println("===========================[Session Key Result]===========================");
    System.out.println("[INFO] made SessionKey : " + madesskey);    
    System.out.println();
    IntegrationClientAPI api = new IntegrationClientAPI("D:\\esbclient.properties");

    // 웹 서비스 활용자 인증처리를 한다.
    System.out.println("---------------------------------------------");
    api.auth(UserType.USER);

    // 웹 서비스 활용자를 위한 서비스 인증키가 생성된다.
    String madesskey = api.makeMobileKey("A12345", "test");

    // 웹 서비스 활용자를 위한 서비스 인증키(session key)를 발행한 후 인증 서버에 등록한다.
    api.sendSessionKey(madesskey);
    
    System.out.println("===========================[Session Key Result]===========================");
    System.out.println("[INFO] made SessionKey : " + madesskey);    
    System.out.println();

    /*-------------------------------------------------------------------------------------*/
    // REST 웹서비스 호출 GET
    /*-------------------------------------------------------------------------------------*/
    wstype = WebSvcType.REST; // REST Type


    //입력값
    //버스노선ID busRouteId : 100100223
    URI = "http://ws.bus.go.kr/api/rest/buspos/getBusPosByRtid?busRouteId=100100223"; // URI을 설정한다.
    filename = null; // GET 요청은 요청 파라미터가 필요없음
    reqStr = null;

    // REST 일 경우에는 Message Header를 설정한다.
    // SOAP 일 경우에는 SOAP 메시지에 Message Header를 작성한다.
    headerCnt = api.setHeaderCnt("serviceKey", "requestTime", "callbackURI", "reqMsgID");

    // REST 웹 서비스를 호출한다.
    retval = api.send(wstype, URI, reqStr, headerCnt);
    
    // 반환값을 출력한다.
    System.out.println("===========================[REST GET Request Result]===========================");
    System.out.println(retval);
    System.out.println();
                
}
