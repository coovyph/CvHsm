package com.company.cvhsm.grpc;

import java.util.concurrent.CompletableFuture;

import com.company.cvhsm.exception.HsmErrorCodeException;
import com.company.cvhsm.hsm.HsmCommand;
import com.company.cvhsm.hsm.HsmCommandFactory;
import com.company.cvhsm.hsm.HsmCommandInvoker;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC service that converts incoming requests into Thales HSM host commands and returns the responses.
 */
@GrpcService
@Log4j2
@RequiredArgsConstructor
public class HsmGrpcService extends HsmServiceGrpc.HsmServiceImplBase {

	private final HsmCommandInvoker hsmCommandInvoker;
	private final HsmCommandFactory hsmCommandFactory;

	@Override
	public void generateKey(GenerateKeyRequest request, StreamObserver<GenerateKeyResponse> responseObserver) {
		HsmCommand<GenerateKeyRequest, GenerateKeyResponse> command = hsmCommandFactory.getCommand("A0");
		hsmCommandInvoker.invoke(request, command)
		.thenAccept(response -> {
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		})
		.exceptionally(throwable -> {
			responseObserver.onError(toGrpcError(throwable));
			return null;
		});
	}

	@Override
	public void generateKcv(com.company.cvhsm.grpc.GenKcvRequest request,
			io.grpc.stub.StreamObserver<com.company.cvhsm.grpc.GenKcvResponse> responseObserver) {
		HsmCommand<GenKcvRequest, GenKcvResponse> command = hsmCommandFactory.getCommand("BU");
		hsmCommandInvoker.invoke(request, command)
		.thenAccept(response -> {
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		})
		.exceptionally(throwable -> {
			responseObserver.onError(toGrpcError(throwable));
			return null;
		});
	}

	@Override
	public void importKey(ImportKeyRequest request, StreamObserver<ImportKeyResponse> responseObserver) {
		HsmCommand<ImportKeyRequest, ImportKeyResponse> command = hsmCommandFactory.getCommand("A6");
		hsmCommandInvoker.invoke(request, command)
		.thenAccept(response -> {
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		})
		.exceptionally(throwable -> {
			responseObserver.onError(toGrpcError(throwable));
			return null;
		});
	}

	@Override
	public void exportKey(ExportKeyRequest request, StreamObserver<ExportKeyResponse> responseObserver) {
		HsmCommand<ExportKeyRequest, ExportKeyResponse> command = hsmCommandFactory.getCommand("A8");
		CompletableFuture<ExportKeyResponse> cf = null;
		try {
			cf = hsmCommandInvoker.invoke(request, command);
		}catch(IllegalArgumentException ie) {
			responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(ie.getMessage()).asRuntimeException());
			return;
		}
		cf.thenAccept(response -> {
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}).exceptionally(throwable -> {
			responseObserver.onError(toGrpcError(throwable));
			return null;
		});
	}

	@Override
	public void translatePinFromTpkToZpk(TranslatePinRequest request,
			StreamObserver<TranslatePinResponse> responseObserver) {
		HsmCommand<TranslatePinRequest, TranslatePinResponse> command =
				hsmCommandFactory.getCommand("CA");
		hsmCommandInvoker.invoke(request, command)
		.thenAccept(response -> {
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		})
		.exceptionally(throwable -> {
			responseObserver.onError(toGrpcError(throwable));
			return null;
		});
	}

	@Override
	public void translatePINFromZPK(TranslatePinRequest request,
			StreamObserver<TranslatePinResponse> responseObserver) {
		HsmCommand<TranslatePinRequest, TranslatePinResponse> command =
				hsmCommandFactory.getCommand("CC");

		hsmCommandInvoker.invoke(request, command)
		.thenAccept(response -> {
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		})
		.exceptionally(throwable -> {
			responseObserver.onError(toGrpcError(throwable));
			return null;
		});
	}

	public void hsmStatus(com.company.cvhsm.grpc.HsmStatusRequest request,
			io.grpc.stub.StreamObserver<com.company.cvhsm.grpc.HsmStatusResponse> responseObserver) {
		HsmCommand<HsmStatusRequest, HsmStatusResponse> command =
				hsmCommandFactory.getCommand("NO");

		hsmCommandInvoker.invoke(request, command)
		.thenAccept(response -> {
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		})
		.exceptionally(throwable -> {
			responseObserver.onError(toGrpcError(throwable));
			return null;
		});
	}

	private static Throwable toGrpcError(Throwable throwable) {
		log.debug("ERror {}",throwable.getMessage());
		if(throwable instanceof IllegalArgumentException ie) {
			return Status.INVALID_ARGUMENT.withDescription(ie.getMessage()).asRuntimeException();
		}
		Throwable cause = throwable.getCause();
		if (cause == null) {
			cause = throwable;
		}

		if (cause instanceof HsmErrorCodeException hsmError) {
			return Status.UNKNOWN
					.withDescription(String.format("HSM returned error code %s for response %s",
							hsmError.getErrorCode(), hsmError.getResponseCode()))
					.asRuntimeException();
		}

		return Status.INTERNAL
				.withDescription(cause.getMessage())
				.asRuntimeException();
	}
}
