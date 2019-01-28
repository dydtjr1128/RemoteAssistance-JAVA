# RemoteAssistance
`RemoteAssistance` like TeamViewer(JAVA)

https://dydtjr1128.github.io/RemoteAssistance-JAVA/.

키보드, 마우스 후킹(JNI)을 이용한 원격지원 프로그램


## Sample

![default](https://user-images.githubusercontent.com/19161231/50884683-01ef7700-142f-11e9-8ba0-82c4dd2ed735.gif)

## Simple Structure
<p>
  <img src="https://user-images.githubusercontent.com/19161231/48710563-1fcd0680-ec4c-11e8-8620-7709af3418f6.png" width="50%">
</p>


## ToDo

<p>
  <ul>
    <li>- [ ] Reduce network traffic</li>
    <li>- [ ] Improve image compress speed(ex. double buffering, grid image)</li>
    <li>- [ ] Encryption data</li>    
    <li>- [ ] Compare using SIMD</li>
    <li>- [ ] Communication with central server</li>
    <li>- [ ] GUI</li>
    <li>- [ ] Send System sound</li>
    <li>- [ ] Multi user session</li>
    <li>- [ ] Chatting & Voice chat</li>    
    <li>- [ ] File manager</li>
    <li>- [ ] Install program</li>
    <li>- [ ] Service registration(Windows)</li>
    <li>- [ ] Support Linux</li>
    <li>- [ ] Plan a connection flow
      <ul>
        <li>client - broker - host</li>
      </ul>
    </li>
  </ul>
</p>
<p>
  <img src="https://user-images.githubusercontent.com/19161231/48710631-5440c280-ec4c-11e8-9808-39203fa8d10b.png" width="50%">
</p>

## Notice
- JAVA의 BufferedImage 메모리 제거 문제로 인하여 C++로 재개발
- https://github.com/dydtjr1128/RemoteAssistance-Cpp

## 후기
이번 원격지원 프로그램을 만들어보면서 네트워크 적인 측면에서나 스크린 캡쳐부분이 굉장히 취약한 부분이 많았다는 것을 알 수 있었다.



</br> 
<a href="mailto:dydtjr1994@gmail.com" target="_blank">
  <img src="https://img.shields.io/badge/E--mail-Yongseok%20choi-yellow.svg">
</a>
<a href="https://blog.naver.com/cys_star" target="_blank">
  <img src="https://img.shields.io/badge/Blog-cys__star%27s%20Blog-blue.svg">
</a>
