# การพัฒนาระบบสั่งอาหารผ่านสมาร์ทโฟนโดยใช้เทคโนโลยี NFC
Fast Order เป็น โมบายแอพพลิเคชั่น ที่ช่วยการจัดการในการสั่งอาหารโดยผ่านNFC บนสมาร์ทโฟน โดยมีการพัฒนาขึ้นบนระบบปฏิบัติการวินโดวส์ โดยใช้เครื่องมือในการพัฒนา คือ Android Studio โดยใช้ภาษา Kotlin ในการเขียนโปรแกรมร่วมกับระบบฐานข้อมูล Firebase Realtime Database

## ขอบเขตด้านกระบวนการทำงานของระบบ
* การสั่งอาหาร  ลูกค้าสามารถสั่งอาหารได้โดยไม่ต้องรอพนักงานมารับออเดอร์เพียงใช้ Smart phone ของลูกค้าแล้วดูเมนูที่ทางร้านเตรียมไว้ให้ โดยใช้เทคโนโลยี QR-Code และ NFC ให้ลูกค้าสแกนเพื่อสั่งอาหารผ่านแอพพลิเคชั่นได้โดยตรง
* การเช็คสถานะของอาหาร  เมื่อสั่งอาหารไปแล้วลูกค้าสามารถดูสถานะของอาหารที่สั่งไปได้ว่ากำลังอยู่ในช่วงไหนทำแล้วหรือยังต้องใช้เวลาเท่าไหร่ ถ้าหากลูกค้าเกิดอยากยกเลิกอาหารที่ต้องการก็สามารถทำได้
* การตรวจสอบรายการอาหารและยอดชำระ เมื่อลูกค้าได้รับอาหารที่สั่งแล้วสามารถตรวจสอบรายการอาหารและค่าอาหารได้ผ่านแอพพลิเคชั่นโดยแอพพลิเคชั่นจะทำการสรุปยอดชำระทั้งหมดมาให้
* การชำระเงิน เมื่อลูกค้าทำการเช็ครายการอาหารทั้งหมดเรียบร้อยแล้ว ลูกค้าสามารถชำระเงินได้ผ่านแอพพลิเคชั่นซึ่งในส่วนนี้จะมีการชำระทั้งแบบ PayPal และ visa เมื่อชำระเสร็จแล้วลูกค้าจะได้ใบเสร็จเพื่อยืนยันว่าได้ทำการชำระเงินเรียบร้อยแล้ว

## ขอบเขตด้านเทคโนโลยีของระบบ
* Android Studio 3.0.1
* Adobe Photoshop CS6
* GenyMotion
* RFID หรือ NFC