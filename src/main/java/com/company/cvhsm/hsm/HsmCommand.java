package com.company.cvhsm.hsm;

/**
 * Defines how a gRPC request is packed into an HSM command body and how an HSM response body is unpacked
 * into a gRPC response.
 *
 * @param <Req>  the gRPC request type
 * @param <Resp> the gRPC response type
 */
public interface HsmCommand<Req, Resp> {

    /**
     * Returns the 2-character HSM command code (e.g., "B2" for echo, "A0" for generate key).
     */
    String getCommandCode();

    /**
     * Returns the expected 2-character HSM response code (e.g., "B3" for echo, "A1" for generate key).
     */
    String getResponseCode();

    /**
     * Packs the gRPC request into the HSM command body bytes (everything after the 2-char command code).
     */
    byte[] packRequestBody(Req request);

    /**
     * Unpacks the HSM response body bytes into the gRPC response type.
     *
     * @param errorCode the 2-character HSM error code
     * @param body      the raw response body bytes (after the response code and error code)
     * @return the gRPC response object
     */
    Resp unpackResponseBody(String errorCode, Object cache,byte[] body);
    
    Object createCache(Req request);
}
