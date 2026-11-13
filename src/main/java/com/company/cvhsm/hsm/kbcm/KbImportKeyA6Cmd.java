package com.company.cvhsm.hsm.kbcm;

import org.springframework.stereotype.Service;

import com.company.cvhsm.config.OnKeyblock;
import com.company.cvhsm.grpc.ImportKeyRequest;
import com.company.cvhsm.hsm.ImportKeyA6Cmd;

@Service
@OnKeyblock
public class KbImportKeyA6Cmd extends ImportKeyA6Cmd{

	@Override
	protected void packAdditionalBytes(ImportKeyRequest request, byte[] rslt, int offset) {
		//request.getKEUSage
		rslt[offset]='#';
		offset++;

		byte[] tmp = request.getKeyUsage().getBytes();
		System.arraycopy(tmp, 0, rslt, offset, tmp.length);
		offset+=tmp.length;
		System.out.println("Mode of use : " + request.getModeOfUse());

		rslt[offset] = (byte)request.getModeOfUse().charAt(0);
		offset++;

		tmp = request.getKeyVersionNumber().getBytes();
		System.arraycopy(tmp, 0, rslt, offset, tmp.length);
		offset+=tmp.length;

		tmp = request.getExportability().getBytes();
		System.arraycopy(tmp, 0, rslt, offset, tmp.length);
		offset+=tmp.length;

		rslt[offset] = 0x30;
		rslt[offset+1] = 0x30;

	}

	@Override
	protected int totalPackBytes(ImportKeyRequest request) {
		// key type + lmk ke scheme
		int lgth = 4;

		lgth+=keyBlockLength(request.getZmk())+1;
		
		int size =keyHexLength(request.getKeyUnderZmk().charAt(0)) + 1;
		if(size == 17 && request.getZmk().charAt(0)!='Z') {
			size--;
		}
		lgth+=size;

		// key block options 9 char
		lgth+=9;

		return lgth;
	}

}
