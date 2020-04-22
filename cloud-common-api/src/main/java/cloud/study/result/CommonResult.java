package cloud.study.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * @author hj
 * 2019-05-24 17:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonResult implements Serializable {

    private Object data;
    private Integer code;
    private String message;

    public static  CommonResult ok(Object data) {
        return CommonResult.builder()
                .code(200)
                .data(data)
                .message("success")
                .build();
    }

    public static  CommonResult fail(String errorMsg, Integer code) {
        return CommonResult.builder()
                .code(code)
                .message(errorMsg)
                .build();
    }

    public static  CommonResult fail(Object data, String errorMsg, Integer code) {
        return CommonResult.builder()
                .code(code)
                .data(data)
                .message(errorMsg)
                .build();
    }

}
