package org.yzh.jt808.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.yzh.framework.commons.transform.JsonUtils;
import org.yzh.framework.message.PackageData;
import org.yzh.web.config.Charsets;
import org.yzh.web.jt808.codec.JT808MessageDecoder;
import org.yzh.web.jt808.codec.JT808MessageEncoder;
import org.yzh.web.jt808.dto.*;
import org.yzh.web.jt808.dto.basics.Header;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * JT/T 808协议单元测试类
 *
 * @author zhihao.ye (yezhihaoo@gmail.com)
 */
public class CoderTest {

    private static final JT808MessageDecoder decoder = new JT808MessageDecoder(Charsets.GBK);

    private static final JT808MessageEncoder encoder = new JT808MessageEncoder(Charsets.GBK);

    public static <T extends PackageData> T transform(Class<T> clazz, String hex) {
        ByteBuf buf = Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(hex));
        PackageData<Header> body = decoder.decode(buf, Header.class, clazz);
        return (T) body;
    }

    public static String transform(PackageData<Header> packageData) {
        ByteBuf buf = encoder.encode(packageData);
        String hex = ByteBufUtil.hexDump(buf);
        return hex;
    }

    public static void selfCheck(Class<? extends PackageData> clazz, String hex1) {
        PackageData bean1 = transform(clazz, hex1);

        String hex2 = transform(bean1);
        PackageData bean2 = transform(clazz, hex2);

        String json1 = JsonUtils.toJson(bean1);
        String json2 = JsonUtils.toJson(bean2);
        System.out.println(hex1);
        System.out.println(hex2);
        System.out.println(json1);
        System.out.println(json2);
        System.out.println();

        assertEquals("hex not equals", hex1, hex2);
        assertEquals("object not equals", json1, json2);
    }

    public static void selfCheck(PackageData<Header> bean1) {
        String hex1 = transform(bean1);

        PackageData bean2 = transform(bean1.getClass(), hex1);
        String hex2 = transform(bean2);

        String json1 = JsonUtils.toJson(bean1);
        String json2 = JsonUtils.toJson(bean2);
        System.out.println(hex1);
        System.out.println(hex2);
        System.out.println(json1);
        System.out.println(json2);
        System.out.println();

        assertEquals("hex not equals", hex1, hex2);
        assertEquals("object not equals", json1, json2);
    }

    public static Header header() {
        Header header = new Header();
        header.setType(125);
        header.setBodyProperties(1);
        header.setMobileNumber("018276468888");
        header.setSerialNumber(125);
        header.setEncryptionType(0);
        header.setReservedBit(0);
        return header;
    }


    // 位置信息汇报 0x0200
    @Test
    public void testPositionReport() {
        String hex1 = "0200006a064762924976014d000003500004100201d9f1230743425e000300a6ffff190403133450000000250400070008000000e2403836373733323033383535333838392d627566322d323031392d30342d30332d31332d33342d34392d3735372d70686f6e652d2e6a706700000020000c14cde78d";
        selfCheck(PositionReport.class, hex1);
    }


    // 终端注册应答 0x8100
    @Test
    public void testRegisterResult() {
        selfCheck(RegisterResult.class, "8100000306476292482425b4000201cd");
    }


    // 终端注册 0x0100
    @Test
    public void testRegister() {
        selfCheck(PositionReport.class, "0100002e064762924824000200000000484f4f5000bfb5b4ef562d31000000000000000000000000000000015a0d5dff02bba64450393939370002");
        selfCheck(register());
    }

    public static PackageData<Header> register() {
        Register b = new Register();
        b.setHeader(header());
        b.setProvinceId(44);
        b.setCityId(307);
        b.setManufacturerId("测试");
        b.setTerminalType("TEST");
        b.setTerminalId("粤B8888");
        b.setLicensePlateColor(0);
        b.setLicensePlate("粤B8888");
        return b;
    }


    // 提问下发 0x8302
    @Test
    public void testQuestionMessage() {
        selfCheck(QuestionMessage.class, "8302001a017701840207001010062c2c2c2c2c2101000331323302000334353603000337383954");

        selfCheck(questionMessage());
    }

    public static PackageData<Header> questionMessage() {
        QuestionMessage bean = new QuestionMessage();
        List<QuestionMessage.Option> options = new ArrayList();
        bean.setHeader(header());

        bean.buildSign(new int[]{1});
        bean.setContent("123");
        bean.setOptions(options);

        options.add(new QuestionMessage.Option(1, "asd1"));
        options.add(new QuestionMessage.Option(2, "zxc2"));
        return bean;
    }


    // 设置电话本 0x8401
    @Test
    public void testPhoneBook() {
        selfCheck(PhoneBook.class, "0001002e02000000001500250203020b043138323137333431383032d5c5c8fd010604313233313233c0eecbc4030604313233313233cdf5cee535");

        selfCheck(phoneBook());
    }

    public static PhoneBook phoneBook() {
        PhoneBook bean = new PhoneBook();
        bean.setHeader(header());
        bean.setType(PhoneBook.Append);
        bean.add(new PhoneBook.Item(2, "18217341802", "张三"));
        bean.add(new PhoneBook.Item(1, "123123", "李四"));
        bean.add(new PhoneBook.Item(3, "123123", "王五"));
        return bean;
    }


    // 事件设置 0x8301
    @Test
    public void testEventSetting() {
        selfCheck(EventSetting.class, "83010010017701840207000c0202010574657374310205746573743268");

        selfCheck(eventSetting());
    }

    public static EventSetting eventSetting() {
        EventSetting bean = new EventSetting();
        bean.setHeader(header());
        bean.setType(EventSetting.Append);
        bean.addEvent(1, "test");
        bean.addEvent(2, "测试2");
        bean.addEvent(3, "t试2");
        return bean;
    }


    // 终端&平台通用应答 0x0001 0x8001
    @Test
    public void testCommonResult() {
        selfCheck(CommonResult.class, "0001000501770184020701840038810300cd");
    }


    // 终端心跳 0x0002
    @Test
    public void testTerminalHeartbeat() {
        selfCheck(TerminalHeartbeat.class, "00020000064762924976042fa7");
    }


    // 文本信息下发 0x8300
    @Test
    public void testTextMessage() {
        selfCheck(TextMessage.class, "830000050647629242562692015445535480");
    }


    // 摄像头立即拍摄命令 0x8801
    @Test
    public void testCameraShot() {
        selfCheck(cameraShot());
        selfCheck(CameraShot.class, "8801000c0641629242524a43010001000a0001057d017d017d017d0125");
    }

    public static CameraShot cameraShot() {
        CameraShot bean = new CameraShot();
        bean.setHeader(header());
        bean.setChannelId(125);
        bean.setCommand(1);
        bean.setParameter(125);
        bean.setSaveSign(1);
        bean.setResolution(125);
        bean.setQuality(1);
        bean.setBrightness(125);
        bean.setContrast(1);
        bean.setSaturation(125);
        bean.setChroma(1);
        return bean;
    }
}