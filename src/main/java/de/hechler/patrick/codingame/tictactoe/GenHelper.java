package de.hechler.patrick.codingame.tictactoe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("javadoc")
public class GenHelper {
	
	private static final String EOF_TOO_EARLY = "reached EOF too early";
	
	private static final int LARGE = 1 << 25;
	
	private static final Comparator<byte[]> CMP = (a, b) -> {
		int len = a.length;
		if (len != b.length) {
			return len - b.length;
		}
		for (int i = 0; i < len; i++) {
			if (a[i] != b[i]) {
				return a[i] - b[i];
			}
		}
		return 0;
	};
	
	public static void main(String[] args) throws IOException {// NOSONAR
		int  turnNumber = 0;
		Path folder     = Path.of(args[0]);
		Path cur        = folder.resolve(turnNumber + ".data");
		System.out.println("folder: " + folder);
		if (args.length > 1) {
			turnNumber = Integer.parseInt(args[1]);
			if (turnNumber < 0) {
				turnNumber = -turnNumber;
				cur        = folder.resolve(turnNumber + ".data");
			} else {
				cur = folder.resolve(turnNumber + ".data");
				long startPos = args.length > 2 ? Long.parseLong(args[2]) : 0L;
				removeDuplicates(cur, startPos);
			}
		} else {
			putInit(cur);
		}
		args = null;
		Files.createDirectories(folder);
		Path next = folder.resolve(++turnNumber + ".data");
		long ls   = 1;
		do {
			long nls       = 0;
			long startTime = System.currentTimeMillis();
			long fls       = 0L;
			try (InputStream in = new BufferedInputStream(Files.newInputStream(cur));
					OutputStream out = new BufferedOutputStream(Files.newOutputStream(next, StandardOpenOption.APPEND, StandardOpenOption.CREATE))) {
				while (true) {
					TTTField0 f = readNext(in);
					if (f == null) break;
					fls++;
					for (int x = 0; x < 3; x++) {
						for (int y = 0; y < 3; y++) {
							Object v = f.value(x, y);
							if (v instanceof Boolean) continue;
							Boolean[] s = (Boolean[]) v;
							for (int ix = 0, ixx = 0; ix < 3; ix++, ixx += 3) {
								for (int iy = 0; iy < 3; iy++) {
									if (s[ixx + iy] != null) continue;
									int       xc = x * 3 + ix;
									int       yc = y * 3 + iy;
									TTTField0 c  = f.copy();
									c.place(xc, yc, Boolean.TRUE);
									writeField(c, out);
									c = f.copy();
									c.place(xc, yc, Boolean.FALSE);
									writeField(c, out);
									nls += 2;
								}
							}
						}
					}
				}
				System.out.println("turn: " + turnNumber);
				System.out.println("  time: " + (System.currentTimeMillis() - startTime) + " ms");
				System.out.println("  list size: " + ls);
				System.out.println("  list size: " + fls);
				System.out.println("  file size: " + Files.size(cur));
				System.out.println("  free mem:  " + Runtime.getRuntime().freeMemory());
				System.out.println("  total mem: " + Runtime.getRuntime().totalMemory());
				System.out.println("  max mem:   " + Runtime.getRuntime().maxMemory());
			} catch (Throwable t) {
				System.err.println("error:\n" + "turn: " + turnNumber);
				System.err.println("  time: " + (System.currentTimeMillis() - startTime) + " ms");
				System.err.println("  list size: " + ls);
				System.out.println("  list size: " + fls);
				System.out.println("  file size: " + Files.size(cur));
				System.err.println("  next list size: " + nls);
				System.out.println("  next file size: " + Files.size(next));
				System.err.println("  free mem:  " + Runtime.getRuntime().freeMemory());
				System.err.println("  total mem: " + Runtime.getRuntime().totalMemory());
				System.err.println("  max mem:   " + Runtime.getRuntime().maxMemory());
				throw t;
			}
			removeDuplicates(next, 0L);
			ls   = nls;
			cur  = next;
			next = folder.resolve(++turnNumber + ".data");
			Files.deleteIfExists(next);
		} while (ls > 0L);
		System.out.println("finish");
	}
	
	private static void removeDuplicates(Path file, long startPos) throws IOException {
		Map<byte[], byte[]> map = new TreeMap<>(CMP);
		System.out.println(" start filtering file (old size " + Files.size(file) + ")");
		long start  = System.currentTimeMillis();
		long start0 = start;
		try (SeekableByteChannel rc = Files.newByteChannel(file, StandardOpenOption.READ); // do not use a read/write channel, to allow buffering in-/out-put
				SeekableByteChannel wc = Files.newByteChannel(file, StandardOpenOption.WRITE);
				OutputStream w = new BufferedOutputStream(Channels.newOutputStream(wc));
				InputStream in = new IgnoreCloseIn(Channels.newInputStream(rc))) {
			if (startPos != 0) {
				rc.position(startPos);
				wc.position(startPos);
			}
			while (true) { // recreate in every time, since only out has a flush method
				try (BufferedInputStream r = new BufferedInputStream(in)) {
					do { // the map is empty at the start
						int len = r.read();
						if (len == -1) {
							w.flush();// always call flush before the position is used
							wc.truncate(wc.position());
							System.out.println(" finished filtering file (new size " + wc.position() + ") time: " + (System.currentTimeMillis() - start) + " ms");
							return;
						}
						byte[] data = new byte[len];
						read(r, data);
						if (map.putIfAbsent(data, data) == null) {
							w.write(len);
							w.write(data);
						}
					} while (map.size() < LARGE);
					w.flush();// well its not optimal to flush for the position, but calling flush twice each file iteration is acceptable
					final long lmp = wc.position();
					while (true) {
						int len = r.read();
						if (len == -1) {
							w.flush();// always call flush before the position is used
							wc.truncate(wc.position());
							break;
						}
						byte[] data = new byte[len];
						read(r, data);
						if (!map.containsKey(data)) {
							w.write(len);
							w.write(data);
						}
					}
					System.out.println(" filtering file (current size " + wc.position() + ") sub-time: " + (System.currentTimeMillis() - start0) + " ms");
					System.out.println("   start position: " + lmp);
					float percent = (float) (((double) wc.position() / (double) lmp) * 100d); // NOSONAR
					System.out.println("   min percent: " + percent + "%");
					System.out.println("   free mem:  " + Runtime.getRuntime().freeMemory());
					System.out.println("   total mem: " + Runtime.getRuntime().totalMemory());
					System.out.println("   max mem:   " + Runtime.getRuntime().maxMemory());
					map.clear();
					start0 = System.currentTimeMillis();
					// w.flush was already called when the wc.truncate was invoked
					wc.position(lmp);
					rc.position(lmp);
				}
			}
		}
	}
	
	private static void read(InputStream in, byte[] data) throws IOException {
		for (int off = 0; off < data.length;) {
			int r = in.read(data, off, data.length - off);
			if (r == -1) throw new StreamCorruptedException(EOF_TOO_EARLY);
			off += r;
		}
	}
	
	private static TTTField0 readNext(InputStream in) throws IOException {
		int val = in.read();
		if (val == -1) return null;
		byte[] data = new byte[val];
		read(in, data);
		return new TTTField0(data);
	}
	
	private static void writeField(TTTField0 c, OutputStream out) throws IOException {
		byte[] data = c.save();
		if (data.length > 0xFF) {
			throw new AssertionError("save length is too large: " + Integer.toHexString(data.length));
		}
		out.write(data.length);
		out.write(data);
	}
	
	private static void putInit(Path cur) throws IOException {
		try (OutputStream out = Files.newOutputStream(cur)) {
			writeField(new TTTField0(), out);
		}
	}
	
	@SuppressWarnings("unused")
	public static void main1(String[] args) {
		System.out.println(Runtime.getRuntime().availableProcessors());
		System.out.println(BigInteger.valueOf(3L).pow(9 * 9));
		long  cnt = 0L;
		int[] arr = new int[9 * 9];
		int   i   = 0;
		while (true) {
			cnt++;
			if (++arr[i] < 3) {
				i = 0;
				continue;
			}
			arr[i] = 0;
			if (++i > arr.length) {
				System.out.println("cnt: " + cnt);
				return;
			}
		}
	}
	
	private static long cp(TTTField0 f, Object sub0, Boolean v, int sx, int sy) {
		Boolean[] sub = (Boolean[]) sub0;
		long      cnt = 0L;
		for (int x = 0, xx = 0; x < 3; x++, xx += 3) {
			for (int y = 0; y < 3; y++) {
				Boolean s = sub[xx + y];
				if (s == null) {
					TTTField0 cpy = f.copy();
					cnt++;
					if (!cpy.place(x + sx * 3, y + sy * 3, v)) {
						Object  subcpy = cpy.value(x, y);
						Boolean sv     = v.booleanValue() ? Boolean.FALSE : Boolean.TRUE;
						if (subcpy instanceof Boolean) {
							cnt += cp(cpy, sv);
						} else {
							cnt += cp(cpy, subcpy, sv, x, y);
						}
					}
				}
			}
		}
		return cnt;
	}
	
	private static long cp(TTTField0 f, Boolean v) {
		long cnt = 0L;
		for (int bx = 0; bx < 3; bx++) {
			for (int by = 0; by < 3; by++) {
				Object sub0 = f.value(bx, by);
				if (sub0 instanceof Boolean) continue;
				cnt += cp(f, sub0, v, bx, by);
			}
		}
		if (cnt > LARGE) {
			System.err.println("cp" + Thread.currentThread().getName() + " cnt: " + cnt);
		}
		return cnt;
	}
	
}


class IgnoreCloseIn extends InputStream {
	
	private final InputStream in;
	
	public IgnoreCloseIn(InputStream in) {
		this.in = in;
	}
	
	@Override
	public int read() throws IOException { return this.in.read(); }
	
	@Override
	public int read(byte[] b) throws IOException { return this.in.read(b); }
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException { return this.in.read(b, off, len); }
	
	@Override
	public byte[] readAllBytes() throws IOException { return this.in.readAllBytes(); }
	
	@Override
	public byte[] readNBytes(int len) throws IOException { return this.in.readNBytes(len); }
	
	@Override
	public int readNBytes(byte[] b, int off, int len) throws IOException { return this.in.readNBytes(b, off, len); }
	
	@Override
	public long skip(long n) throws IOException { return this.in.skip(n); }
	
	@Override
	public void skipNBytes(long n) throws IOException { this.in.skipNBytes(n); }
	
	@Override
	public int available() throws IOException { return this.in.available(); }
	
	@Override
	public void mark(int readlimit) { this.in.mark(readlimit); }
	
	@Override
	public void reset() throws IOException { this.in.reset(); }
	
	@Override
	public boolean markSupported() { return this.in.markSupported(); }
	
	@Override
	public long transferTo(OutputStream out) throws IOException { return this.in.transferTo(out); }
	
	
	
}

class TTTField0 {
	
	private final Object[] field = new Object[3 * 3];
	
	public TTTField0(byte[] data) {
		int di = 0;
		for (int i = 0; i < 3 * 3; i++) {
			if ((data[di >>> 3] & (1 << (di & 7))) != 0) {// NOSONAR
				di++;
				this.field[i] = (data[di >>> 3] & (1 << (di & 7))) != 0 ? Boolean.TRUE : Boolean.FALSE;// NOSONAR
				di++;
			} else {
				di++;
				Boolean[] arr = new Boolean[3 * 3];
				this.field[i] = arr;
				for (int ii = 0; ii < 3 * 3; ii++) {
					if ((data[di >>> 3] & (1 << (di & 7))) != 0) {// NOSONAR
						di++;
						arr[ii] = (data[di >>> 3] & (1 << (di & 7))) != 0 ? Boolean.TRUE : Boolean.FALSE;// NOSONAR
					}
					di++;
				}
			}
		}
	}
	
	public TTTField0() {
		for (int i = 0; i < 3 * 3; i++) {
			this.field[i] = new Boolean[3 * 3];
		}
	}
	
	public byte[] save() {// NOSONAR
		int bitcnt = 9;
		for (int i = 0; i < 9; i++) {
			if (this.field[i] instanceof Boolean[] arr) {
				bitcnt += 9;
				for (Boolean b : arr) {
					if (b != null) bitcnt++;
				}
			} else {
				bitcnt++;
			}
		}
		byte[] data = new byte[(bitcnt + (Byte.SIZE - 1)) / Byte.SIZE];
		int    di   = 0;
		int    ds   = 0;
		for (int i = 0; i < 9; i++) {
			if (this.field[i] instanceof Boolean b) {
				data[di] |= 1 << ds;
				ds++;
				if (ds == 8) {
					ds = 0;
					di++;
				}
				if (b.booleanValue()) {
					data[di] |= 1 << ds;
				}
			} else {
				ds++;
				if (ds == 8) {
					ds = 0;
					di++;
				}
				for (Boolean b : (Boolean[]) this.field[i]) {
					if (b != null) {
						data[di] |= 1 << ds;
						ds++;
						if (ds == 8) {
							ds = 0;
							di++;
						}
						if (b.booleanValue()) {
							data[di] |= 1 << ds;
						}
						ds++;
						if (ds == 8) {
							ds = 0;
							di++;
						}
					} else {
						ds++;
						if (ds == 8) {
							ds = 0;
							di++;
						}
					}
				}
			}
		}
		return data;
	}
	
	TTTField0(Object[] copyField) {
		for (int i = 0; i < 3 * 3; i++) {
			Object val = copyField[i];
			if (val instanceof Boolean) {
				this.field[i] = val;
			} else {
				this.field[i] = ((Boolean[]) val).clone();
			}
		}
	}
	
	public Object value(int x, int y) {
		return this.field[x * 3 + y];
	}
	
	public boolean place(int lx, int ly, Boolean val) {
		int dx = lx / 3;
		int mx = lx % 3;
		int dy = ly / 3;
		int my = ly % 3;
		int fc = dx * 3 + dy;
		((Boolean[]) this.field[fc])[mx * 3 + my] = val;
		if (checkWon((Boolean[]) this.field[fc], mx, my)) {
			this.field[fc] = val;
			return checkWon(this.field, dx, dy);
		}
		return false;
	}
	
	private static boolean checkWon(Object[] sub, int x, int y) {
		Object c = sub[x * 3 + y];
		// check horizontal
		// check vertical
		// check diagonal \
		// check diagonal /
		final int oc = (x << 4) | y;
		l0: {// NOSONAR
			for (int xx = 0; xx < 9; xx += 3) {
				Object b = sub[xx + y];
				if (c != b) {// NOSONAR // we always use Boolean.TRUE and Boolean.FALSE, so ==/!= is possible
					break l0;
				}
			}
			return true;
		}
		l1: {// NOSONAR
			for (y = 0, x *= 3; y < 3; y++) {
				Object b = sub[x + y];
				if (c != b) {// NOSONAR // we always use Boolean.TRUE and Boolean.FALSE, so ==/!= is possible
					break l1;
				}
			}
			return true;
		}
		l2: switch (oc) {// NOSONAR
		case 0x00, 0x11, 0x22:
			for (x = 0, y = 0; x < 9; x += 3, y++) {
				Object b = sub[x + y];
				if (c != b) {// NOSONAR // we always use Boolean.TRUE and Boolean.FALSE, so ==/!= is possible
					break l2;
				}
			}
			return true;
		}
		switch (oc) {// NOSONAR
		case 0x02, 0x11, 0x20:
			for (x = 0, y = 2; x < 9; x += 3, y--) {
				Object b = sub[x + y];
				if (c != b) {// NOSONAR // we always use Boolean.TRUE and Boolean.FALSE, so ==/!= is possible
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	public TTTField0 copy() {
		return new TTTField0(this.field);
	}
	
	@Override
	public int hashCode() {
		final int prime  = 31;
		int       result = 1;
		result = prime * result + Arrays.deepHashCode(this.field);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TTTField0)) return false;
		TTTField0 other = (TTTField0) obj;
		if (!Arrays.deepEquals(this.field, other.field)) return false;
		return true;
	}
	
}
