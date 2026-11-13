# Generate a Key

**Document:** payShield® 10K Core Host Commands  
**Command:** `A0`  
**Response:** `A1`  
**Source page range:** 39–44  
**Document number:** PUGD0537-003

## Function

To generate a random key or a derived key and return it encrypted under the LMK and optionally under a ZMK or TMK (for transmission to another party).

## Authorization

| Key Scheme (ZMK) | Authorization |
|---|---|
| No export | None |
| `S` (Thales Key Block) | None |
| `R` (TR-31 Key Block) | None |
| `U`, `T` (Variant) | Required |
| `Z`, `X`, `Y` (X9.17) | Required |

## Command message

| Field | Length & Type | Details |
|---|---:|---|
| Message Header | `m A` | Subsequently returned to the Host unchanged. |
| Command Code | `2 A` | Value `A0`. |
| Mode | `1 H` | Indicates the operation of the function: `0` = Generate key; `1` = Generate key and encrypt under ZMK (or TMK or Current BDK); `A` = Derive key; `B` = Derive key and encrypt under ZMK (or TMK or Current BDK). |
| Key Type | `3 H` | For a Variant LMK, indicates the key type of the key to be generated/derived. When generating a new key (Mode `0` or `1`), see **Available Key Types/Usages** (p. 37). When deriving a key from a master key (Mode `A` or `B`), the available values are listed below. For a Key Block LMK, this field is ignored and should be set to `FFF`. |
| Key Scheme (LMK) | `1 A` | Indicates the scheme for encrypting the output key under the LMK. See the Key Scheme Table in *key-schemes.md* . |
| Derive Key Mode | `1 A` | Present only if Mode is `A` or `B`. Indicates the operation to derive an encryption key: `0` = DUKPT — derive IKEY from DUKPT Master Key; `1` = ZKA — derive ZKA key from ZKA Master Key. |

### Key Type values when deriving a key

| Key Type | Description | Derive Key Mode |
|---|---|---:|
| `302` | IKEY | `0` (DUKPT) |
| `001` | ZPK | `1` (ZKA) |
| `008` | ZAK | `1` (ZKA) |
| `00A` | ZEK | `1` (ZKA) |

### Additional fields for DUKPT derivation

| Field | Length & Type | Details |
|---|---:|---|
| DUKPT Master Key Type | `1 H` | Present only if Derive Key Mode is `0`. Indicates the DUKPT Master Key Type for deriving the initial device key: `1` = BDK-1; `2` = BDK-2; `3` = BDK-3; `4` = BDK-4; `5` = BDK-5. |
| DUKPT Master Key | `'U' + 32 H` or `'T' + 48 H`, or `'S' + n A` | Present only if Derive Key Mode is `0`. The DUKPT Master Key (BDK). For a Variant LMK: BDK-1 is encrypted under LMK 28/29; BDK-2 under LMK 28/29 variant 6; BDK-3 under LMK 28/29 variant 8; BDK-4 under LMK 28/29 variant 9. BDK-5 can only be encrypted using Key Block, Key Usage `44`. For a Key Block LMK, the BDK must comply with the following Key Block attributes: Key Usage `B0`, `41`, `42`, `43`, or `44`; Algorithm `T` or `A`; Mode of Use `X` or `N`. |
| KSN | `15 H` or `16 H` | Present only if Derive Key Mode is `0`. A Key Set Identifier and Device ID for deriving the Initial Key. Right-justified and padded with `F`s. There is no Transaction Counter, unlike a KSN sent in transaction data from a terminal. For a 3DES BDK, the KSN is `15 H`. Example: for KSI + DID `303950+12342468` (14 hex characters), the KSN is `F30395012342468`; the last hex character (`8`) must be even. For an AES BDK, the KSN is `16 H`. Example: for BDK ID + DID `30395059 + 12345678` (16 hex characters), the KSN is `3039505912345678`; the last character does not need to be even. See the Host Programmer's Manual for further information. |

### Additional fields for ZKA derivation

| Field | Length & Type | Details |
|---|---:|---|
| ZKA Master Key Type | `3 H` | Present only if Derive Key Mode is `1`. For a Variant LMK, indicates the LMK pair/variant under which the ZKA Master Key is encrypted: `607` = Master Key is encrypted under LMK pair 24-25/6. For a Key Block LMK, this field is ignored and should be set to `FFF`. |
| ZKA Master Key | `32 H`, `'U' + 32 H`, or `'S' + n A` | Present only if Derive Key Mode is `1`. The ZKA Master Key must be a double-length DES key. For a Variant LMK, it is encrypted under the LMK pair/variant indicated by the ZKA Master Key Type field. For a Key Block LMK, it must comply with: Key Usage `53`; Algorithm `T`; Mode of Use `X` or `N`. |
| ZKA Option | `1 H` | Present only if Derive Key Mode is `1`. `0` = Derive key using supplied RNDI; `1` = Derive key using new RNDI. |
| ZKA RNDI | `32 H` | Present only if ZKA Option is `0`. Random Number Input used to derive the PAC/MAC/DE key from the ZKA Master Key. |

### Optional export-under-ZMK/TMK fields

| Field | Length & Type | Details |
|---|---:|---|
| Delimiter | `1 A` | Optional. Value `;`. Can only be present if Mode is `1` or `B`. If present, the following field must be present. |
| ZMK/TMK Flag | `1 N` | Optional; present only if the preceding delimiter is present. `0` = ZMK (default if these fields are not present); `1` = TMK. |
| ZMK (or TMK or Current BDK) | `16 H` / `32 H`, `'U' + 32 H`, `'T' + 48 H`, or `'S' + n A` | Present only if Mode is `1` or `B`. The Zone Master Key (or Terminal Master Key or Current Base Derivation Key). For a ZMK, the key length must correspond to the ZMK key length defined in the Security Settings. For a Variant LMK, the ZMK is encrypted under LMK pair 04-05. The TMK is encrypted under LMK pair 14-15 variant 0 if the security setting **Enforce key type 002 separation for PCI HSM compliance** is `N`, or LMK pair 36-37 variant 8 if the setting is `Y`. For a Key Block LMK, the ZMK/TMK must comply with the attributes below. |
| Current BDK's Initial Key Serial Number | `16 H` | Only present if using a current BDK. The key serial number of the current BDK. |
| Key Scheme (ZMK or TMK or Current BDK) | `1 A` | Present only if Mode is `1` or `B`. Indicates the scheme for encrypting the key under the ZMK (or TMK or Current BDK). See the Key Scheme Table in *key-schemes.md* . |
| Atalla Variant | `1 / 2 N` | Present only if Mode is `1` or `B`. Optional. Atalla variant; for use in systems with Atalla equipment. |
| Delimiter | `1 A` | Optional. Value `%`. If present, the following field must be present. |
| LMK Identifier | `2 N` | LMK identifier; minimum value `00`; maximum value is defined by the license. Must be present if the preceding delimiter is present. |

#### Key Block attributes for ZMK/TMK

| Key Usage | Algorithm | Mode of Use |
|---|---|---|
| `K0`, `51`, `52` | `D`, `T`, `A` | `B`, `E`, `N` |
| `B0`, `41`, `43` | `A` | `X`, `N` |

### Key Block generation and export fields

The following fields apply only when generating and optionally exporting in Key Block format.

| Field | Length & Type | Details |
|---|---:|---|
| Delimiter | `1 A` | Required when generating a key block. Value `#`. If present, the following fields must be present. |
| Key Usage | `2 A` | Key Usage field to be included in the key block header. See **Available Key Types/Usages** (p. 37). |
| Algorithm | `2 A` | Algorithm and key length; the first character is included in the algorithm field in the key block header (byte 7). Permitted values: `D1` = single-length DES key; `T2` = double-length DES key; `T3` = triple-length DES key; `A1` = 128-bit AES key; `A2` = 192-bit AES key; `A3` = 256-bit AES key. |
| Mode of Use | `1 A` | Mode of Use field to be included in the key block header. Any permitted value for the key type. See the Mode of Use Table in Chapter 8 of the payShield 10K Host Programmer's Manual. |
| Key Version Number | `2 N` | Key Version Number field to be included in the key block header. Permitted values `00` to `99`. |
| Exportability | `1 A` | Exportability field to be included in the key block header. Any permitted value. |
| Number of Optional Blocks | `2 N` | Number of optional blocks specified below. Permitted values `00` to `08`. |
| Optional Block Identifier | `2 A` | For each optional block, specify an identifier. Any permitted value except `PB`. |
| Optional Block Length | `2 H` | For each optional block, number of characters in the optional block, including identifier and length. Permitted values `X'04` to `X'FF`. If the value is `X'04`, the following field is not present. |
| Optional Block Data | `n A` | Optional block data. |

For each optional block, the Optional Block Identifier, Optional Block Length, and Optional Block Data fields must be specified. If Number of Optional Blocks is `00`, none of these three fields should be present.

| Field | Length & Type | Details |
|---|---:|---|
| Delimiter | `1 A` | Optional. Value `&`. Can only be present when the generated key is to be exported. If present, the following field must be present. |
| Modified Export Value | `1 A` | Character to be placed in the exportability field (byte 11) of the exported key. The only permitted value is `N`. Must be present if the preceding delimiter is present. |
| Delimiter | `1 A` | Optional. Value `!`. Can only be present when the generated key is to be exported in TR-31 key block format. If present, the following field must be present. |
| Key Block Version ID | `1 A` | Key Block Version ID: `A` = Key block protected using the Key Variant Binding Method (Ref 8, section 5.3.3.1); `B` = Key Derivation Binding Method (Ref 8, section 5.3.2.1); `C` = Key Variant Binding Method (Ref 8, section 5.3.3.1); `D` = AES Key Derivation Binding Method (Ref 8, section 5.3.2.3). |

### Variant key export to TR-31 format

The following section applies only when generating a variant key and exporting to TR-31 format.

| Field | Length & Type | Details |
|---|---:|---|
| Delimiter | `1 A` | Optional. Value `&`. Can only be present when the generated key is to be exported. If present, the following fields must be present. |
| Key Usage | `2 A` | Key Usage field to be included in the key block header. Any TR-31 permitted value for the key type. See Chapter 8 of the payShield 10K Host Programmer's Manual. |
| Mode of Use | `1 A` | Mode of Use field to be included in the key block header. Any permitted value for the key type. |
| Key Version Number | `2 N` | Key Version Number field to be included in the key block header. Permitted values `00` to `99`. |
| Exportability | `1 A` | Exportability field to be included in the key block header. Only permitted values are `N` or `S`. |
| Number of Optional Blocks | `2 N` | Number of optional blocks specified below. Permitted values `00` to `02`. Only `KS` and `KV` Optional Blocks are valid. |
| Optional Block Identifier | `2 A` | For each optional block, specify an identifier. Any permitted value except `PB`. |
| Optional Block Length | `2 H` | For each optional block, number of characters in the optional block, including identifier and length. Permitted values `X'04` to `X'FF`. If the value is `X'04`, the following field is not present. |
| Optional Block Data | `n A` | Optional block data. |
| Delimiter | `1 A` | Optional. Value `!`. Can only be present when the generated key is to be exported to TR-31 key block format. If present, the following field must be present. |
| Key Block Version ID | `1 A` | `A` = Key Variant Binding Method (Ref 8, section 5.3.3.1); `B` = Key Derivation Binding Method (Ref 8, section 5.3.2.1); `C` = Key Variant Binding Method (Ref 8, section 5.3.3.1); `D` = AES Key Derivation Binding Method (Ref 8, section 5.3.2.3). |
| End Message Delimiter | `1 C` | Must be present if a message trailer is present. Value `X'19`. |
| Message Trailer | `n A` | Optional. Maximum length 32 characters. |

For each optional block, the Optional Block Identifier, Optional Block Length, and Optional Block Data fields must be specified. If Number of Optional Blocks is `00`, none of these three fields should be present.

## Response message

| Field | Length & Type | Details |
|---|---:|---|
| Message Header | `m A` | Returned to the Host unchanged. |
| Response Code | `2 A` | Value `A1`. |
| Error Code | `2 A` | `00` = No error; `07` = Invalid ZKA Master Key Type; `10` = ZMK or TMK Parity error; `68` = Command disabled; or a standard error code. |
| Key (under LMK) | `16 H`, `'U' + 32 H`, `'T' + 48 H`, or `'S' + n A` | The new key, encrypted under the LMK. For a Variant LMK, it is encrypted under the appropriate LMK pair/variant, determined by the Key Type. For a Key Block LMK, the key is encrypted under the LMK. |
| Key (under ZMK or TMK or Current BDK) | `16 H`, `'U' + 32 H`, `'T' + 48 H`, `'R' + n A`, or `'S' + n A` | Present only if Mode is `1` or `B`. The new key, encrypted under the supplied ZMK (or TMK or Current BDK). |
| Key Check Value | `6 H` | The key check value. |
| ZKA RNDI | `32 H` | Present only if ZKA Option is `1`. Newly generated RNDI used to derive the PAC/MAC/DE key from the ZKA Master Key. |
| End Message Delimiter | `1 C` | Present only if present in the command message. Value `X'19`. |
| Message Trailer | `n A` | Present only if present in the command message. Maximum length 32 characters. |

## Notes

No additional note text is present in the source section under the **Notes** heading.

## Further notes

Exporting to a key block format requires the ZMK/TMK to be one of the following:

- Double- or triple-length DES key.
- Any size AES key.

---

*Images from the source pages have been omitted, as requested.*
