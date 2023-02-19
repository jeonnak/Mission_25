// 프로덕트 클래스 선언

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok. NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionProduct {
    private int no;
    private String name;
    private int price;
    private int stock;
}
