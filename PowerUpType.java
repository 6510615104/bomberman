package Bomberman;

// กำหนดประเภทของ Power-up
public enum PowerUpType {
    BOMB_COUNT, // เพิ่มจำนวนระเบิดที่วางได้พร้อมกัน
    EXPLOSION_RANGE, // เพิ่มรัศมีการระเบิด
    SPEED // เพิ่มความเร็วในการเคลื่อนที่
    // สามารถเพิ่มประเภทอื่นๆ ได้ในอนาคต เช่น KICK_BOMB, DETONATOR
}