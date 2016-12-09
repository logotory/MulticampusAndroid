package com.example.ch6_http;

/**
 * Created by student on 2016-12-09.
 *
 * 라이브러리 개발자가 만든다는 가정..
 * 라이브러리 클래스가 네트워킹 대행.. 결과 데이터를 activity에 전달?
 * 함수를 통일시킬 목적으로 ..
 * Activity개발자가 구현
 */

public interface HttpCallback {

    void onResult(String result);


}
