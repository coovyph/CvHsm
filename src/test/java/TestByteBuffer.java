import java.nio.ByteBuffer;

public class TestByteBuffer {
	public static void main(String[] args) {

		ByteBuffer buff = ByteBuffer.allocate(10);
		buff.put((byte)30);
		buff.put("Hello".getBytes());

		
		byte[] rslt = new byte[buff.position()];
		buff.flip();

		
		buff.get(rslt);

		System.out.println(new String(rslt));
	}
}
