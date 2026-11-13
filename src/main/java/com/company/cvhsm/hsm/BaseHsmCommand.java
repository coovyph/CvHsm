package com.company.cvhsm.hsm;

public abstract class BaseHsmCommand<Req,Resp> implements HsmCommand<Req, Resp>{

	public static final int KCV_LENGTH = 6;

	protected void valueEquals(String value,String fieldName,boolean nullable,String match) {
		if(value==null) {
			if(!nullable) {
				throw new IllegalArgumentException(getCommandCode() + " " + fieldName + " is expected");
			}
			return;
		}

		if(!value.equals(match)) {
			throw new IllegalArgumentException(getCommandCode() + " " + fieldName + " value must be " + match);
		}
	}

	protected void requireIntegerBetween(String value,String fieldName,int start,int end) {
		try {
			int check = Integer.parseInt(value);
			if(start == end && check!=start) {
				throw constructArgumentException(fieldName, "value must be " + start);
			}else if(check < start || check>end) {
				throw constructArgumentException(fieldName, "value must be between " + start + " and " + end);
			}
		}catch(NumberFormatException nfe) {
			throw  constructArgumentException(fieldName, "must be in numeric");
		}
	}

	protected void requireNonEmpty(String value, String fieldName) {
		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException(getCommandCode() + " " + fieldName + " is required");
		}
	}
	protected void valuesIn(String value,String fieldName,boolean nullable,String...values) {
		if(value==null) {
			if(!nullable) {
				throw new IllegalArgumentException(getCommandCode() + " " + fieldName + " is expected");
			}
			return;
		}

		boolean found = false;
		for(String val:values) {
			if(value.equals(val)){
				found = true;
				break;
			}
		}

		if(!found) {
			StringBuilder sb = new StringBuilder();
			sb.append(getCommandCode());
			sb.append(" ");
			sb.append(fieldName);
			sb.append(" value is expected one of ");
			for(int i=0;i<values.length;i++) {
				if(i>0)sb.append("/");
				sb.append(values[i]);
			}
			throw new IllegalArgumentException(sb.toString());
		}
	}

	protected void requireLength(String value, String fieldName, int expectedLength) {
		if (value == null || value.length() != expectedLength) {
			throw new IllegalArgumentException(
					getCommandCode() + " " + fieldName + " must be exactly " + expectedLength + " character(s)");
		}
	}

	protected int keyBlockLength(String mainHeader) {
		try {
			int offset = 1;
			if(mainHeader.startsWith("S"))offset++;
			return Integer.parseInt(mainHeader, offset, offset+4, 10);
		}catch(NumberFormatException ne) {
			return -1;
		}catch(IndexOutOfBoundsException ie) {
			return -1;
		}
	}
	protected int keyHexLength(char scheme) {
		return switch (Character.toUpperCase(scheme)) {
		case 'X' -> 32;
		case 'Y' -> 48;
		case 'U' -> 32;
		case 'T' -> 48;
		default -> 16;

		};
	}

	protected IllegalArgumentException constructArgumentException(String fieldName, String message) {
		StringBuilder sb = new StringBuilder();
		sb.append(getCommandCode());
		sb.append(" ");
		sb.append(fieldName);
		sb.append(" ");
		sb.append(message);
		return new IllegalArgumentException(sb.toString());
	}

	protected void validateKeyFormat(String key, String fieldName,boolean variant) {
		char firstChar = key.charAt(0);
		int expectedLength = 0;

		if(variant) {
			expectedLength = keyHexLength(firstChar) + 1;
			if(expectedLength == 17 && firstChar !='Z') {
				expectedLength -- ;
			}
		}else {
			//key block, the first char must be S
			if(firstChar!='S')throw constructArgumentException(fieldName, " first char must be S");
			expectedLength = keyBlockLength(key) + 1;
		}

		if(key.length()!=expectedLength) 
			throw  constructArgumentException(fieldName, "length must be " + expectedLength + " hex chars");
	}

	@Override
	public Object createCache(Req request) {
		//no need cache
		return null;
	}

}
