# Import a Key

| Variant LMK ☑ | Key Block LMK ☑ |
| --- | --- |
| **Available in package(s): Classic & Premium** |  |
| **Variant LMK** | Authorization: **Determined by KTT(I)**<br>

<br>Activities: **import.{key}.host** |
| **Key Block LMK** | Authorization: **If import from non-KB**<br>

<br>Activity: **import.{key}.host** |

---

**Function:** To import a key encrypted under a ZMK.

**Authorization:**

| Variant LMK | Key Block LMK |
| --- | --- |
| This command examines the 'Import' flag of the given key type within the Key Type Table (see Chapter 7 of the *payShield 10K Host Programmer's Manual*) to determine the authorization requirement. If the flag is 'A', the HSM must either be in the Authorized State, or the activity **import.{key}.host** must be authorized, where 'key' is the key type code of the key being imported. | The authorization requirement for this command depends on the type of import being requested:<br>

<br>

<br>| Scheme of "Key (ZMK)" | Auth |<br>

<br>|--- |

**Notes:**

When importing, the ZMK key block must have the following field values:

| "ZMK" Key Block Header | Value(s) |
| --- | --- |
| Key Usage | 'K0', '52' |
| Algorithm | 'D'*, 'T' |
| Mode of Use | 'B', 'D', 'N' |

* Note: Importing from a key block format is not permitted with a single-length ZMK.

This command cannot be used to import a ZMK.

---

If the option "Enforce Atalla variant match to Thales key type" is set to YES in the CS console command, the following matchings between Atalla variant and Thales variant key types will be enforced:

| Key Type | Atalla Variant | Thales Variant (*) | Thales Variant (ª) |
| --- | --- | --- | --- |
| TPK | 1 or 01 | 002 LMK 14-15 | 70D LMK 36-37/7 |
| ZPK |  | 001 LMK 06-07 | 001 LMK 06-07 |
| ZEK | 2 or 02 | 00B LMK 32-33 | 00B LMK 32-33 |
|  |  | 00A LMK 30-31 | 00A LMK 30-31 |
| TAK | 3 or 03 | 003 LMK 16-17 | 003 LMK 16-17 |
| ZAK |  | 008 LMK 26-27 | 008 LMK 26-27 |
| CVK |  | 402 LMK 14-15/4 | 402 LMK 14-15/4 |
| TMK | 4 or 04 | 002 LMK 14-15 | 80D LMK 36-37/8 |
| TPK |  | 002 LMK 14-15 | 70D LMK 36-37/7 |
| PVK |  | 002 LMK 14-15 | 002 LMK 14-15 |
| TMK | 5 or 05 | 002 LMK 14-15 | 80D LMK 36-37/8 |
| BDK-1 | 8 or 08 | 009 LMK 28-29 | 009 LMK 28-29 |
| MK-AC | 9 or 09 | 109 LMK 28-29/1 | 109 LMK 28-29/1 |
| MK-SMI | 9 or 09 | 209 LMK 28-29/2 | 209 LMK 28-29/2 |
| MK-SMC | 9 or 09 | 309 LMK 28-29/3 | 309 LMK 28-29/3 |
| TEK | 26 | 30B LMK 32-33/3 | 30B LMK 32-33/3 |
| BDK-2 | 30 | 609 LMK 28-29/6 | 609 LMK 28-29/6 |
| BDK-3 | 8 or 08 | 809 LMK 28-29/8 | 809 LMK 28-29/8 |
| BDK-4 | 9 or 09 | 909 LMK 28-29/9 | 909 LMK 28-29/9 |
| BDK-5 | N/A | N/A (Key Block only) | N/A (Key Block only) |

* Applies if the security setting "Enforce key type 002 separation for PCI HSM compliance" has the value "N"

ª Applies if the security setting "Enforce key type 002 separation for PCI HSM compliance" has the value "Y"

**Further Notes:**

In order to import a ZMK, the following security setting must be set:

**Enable import of a ZMK:** Yes

---

## COMMAND MESSAGE

| Field | Length & Type | Details |
| --- | --- | --- |
| Message Header | m A | Subsequently returned to the Host unchanged. |
| Command Code | 2 A | Value 'A6'. |
| Key Type | 3 H | **For a Variant LMK:**<br>

<br>Indicates the key type of the key to be imported. For a list of possible values, see Key Type Codes in Chapter 7 of the *payShield 10K Host Programmer's Manual*.<br>

<br>

<br>**For a Key Block LMK:**<br>

<br>This field is ignored; should be set to 'FFF'. |
| ZMK |  | The ZMK encrypted under the LMK. |
|  | 16 H<br>

<br>or<br>

<br>'U' + 32 H<br>

<br>or<br>

<br>'T' + 48 H | **For a Variant LMK,** the 'ZMK' is encrypted under LMK pair 04-05. |
|  | or<br>

<br>'S' + n A | **For a Key Block LMK,** the 'ZMK' must comply with the following:<br>

<br>| Key Usage | Algorithm | Mode of Use |<br>

<br>|--- |
| Key (ZMK) | 16 H<br>

<br>or<br>

<br>'U' or 'X' + 32 H<br>

<br>or<br>

<br>'T' or 'Y' + 48 H<br>

<br>or<br>

<br>'R' + n A<br>

<br>or<br>

<br>'S' + n A | The Key to be imported, encrypted under the ZMK. The Key can be in X9.17, Variant, TR-31 Key Block, or Thales Key Block format.<br>

<br>If in Thales or TR-31 key block format, the 'Key (ZMK)' must comply with the following:<br>

<br>| Key Usage | Algorithm | Mode of Use |<br>

<br>|--- |
| Key Scheme (LMK) | 1 A | Indicates the scheme for encrypting the key under the LMK. For a list of key schemes, see the Key Scheme Table at Appendix A of the *payShield 10K Host Programmer's Manual*. |
| Atalla Variant | 1 / 2 N | Optional. Atalla variant; for use in systems with Atalla equipment. |
| Delimiter | 1 A | Value '%'. Optional; if present, the following field must be present. |
| LMK Identifier | 2 N | LMK identifier; min value = '00'; max value is defined by license; must be present if the above Delimiter is present. |

The following section applies when importing keys in TR-31 format when using a Key Block LMK.

| Field | Length & Type | Details |
| --- | --- | --- |
| Delimiter | 1 A | Value '#'. Optional; can only be present when the key to be imported is in TR-31 key block format; if present, the following fields must be present. |
| Modified Key Usage | 2 A | Key Usage field, to be included in the imported key block header; must be present if the above Delimiter is present. |
| Number of Optional Blocks | 2 N | Number of Optional Blocks specified below; permitted values '00' to '06'; must be present if the above Delimiter is present. |

For each optional block, the following three fields must be specified.

Note: if the Number of Optional Blocks = '00', then none of the following three fields should be present.

| Field | Length & Type | Details |
| --- | --- | --- |
| Optional Block Identifier | 2 A | Optional Block Identifier; any permitted value except 'KS', 'KV', 'PB'. |
| Optional Block Length | 2 H | Number of characters in the optional block (including identifier and length); permitted values X'04 to X'FF; if value = X'04, then the following field is not present. |
| Optional Block Data | n A | Optional block data. |

The following section applies when importing keys in a non-key block format (e.g. X9.17) when using a Key Block LMK.

| Field | Length & Type | Details |
| --- | --- | --- |
| Delimiter | 1 A | Value '#'. Optional; can only be present when the key to be imported is not in a key block format; if present, the following fields must be present. |
| Key Usage | 2 A | Key Usage field, to be included in the key block header; any permitted value for the key type; must be present if the above Delimiter is present. For a list of possible values, see the Key Usage Table in Chapter 8 of the *payShield 10K Host Programmer's Manual*. |
| Mode of Use | 1 A | Mode of Use field, to be included in the key block header; any permitted value for the key type; must be present if the above Delimiter is present. May be any permitted value for the key type; for a list of possible values, see the Mode of Use Table in Chapter 8 of the *payShield 10K Host Programmer's Manual*. |
| Key Version Number | 2 N | Key Version Number field, to be included in the key block header; permitted values: '00' to '99'; must be present if the above Delimiter is present. |
| Exportability | 1 A | Exportability field, to be included in the key block header; only permitted value is 'N' or 'S'; must be present if the above Delimiter is present. |
| Number of Optional Blocks | 2 N | Number of Optional Blocks specified below; permitted values '00' to '08'; must be present if the above Delimiter is present. |

For each optional block, the following three fields must be specified.

Note: if the Number of Optional Blocks = '00', then none of the following three fields should be present.

| Field | Length & Type | Details |
| --- | --- | --- |
| Optional Block Identifier | 2 A | Optional Block Identifier; any permitted value except 'PB'. |
| Optional Block Length | 2 H | Number of characters in the optional block (including identifier and length); permitted values X'04 to X'FF; if value = X'04, then the following field is not present. |
| Optional Block Data | n A | Optional block data. |
| End Message Delimiter | 1 C | Must be present if a message trailer is present. Value X'19. |
| Message Trailer | n A | Optional. Maximum length 32 characters. |

---

## RESPONSE MESSAGE

| Field | Length & Type | Details |
| --- | --- | --- |
| Message Header | m A | Returned to the Host unchanged. |
| Response Code | 2 A | Value 'A7'. |
| Error Code | 2 A | '00': No error<br>

<br>'01': Key parity error, advice only<br>

<br>'10': ZMK Parity error<br>

<br>'68': Command disabled<br>

<br>or a standard error code.. |
| Key (under LMK) |  | The imported key, encrypted under the LMK. |
|  | 16 H<br>

<br>or<br>

<br>'U' + 32 H<br>

<br>or<br>

<br>'T' + 48 H | **For a Variant LMK,** the 'Key (under LMK)' will be encrypted under the appropriate LMK pair/variant, determined by the 'Key Type'. |
|  | or<br>

<br>'S' + n A | **For a Key Block LMK,** the 'Key (under LMK)' will be encrypted under the LMK. |
| Key Check Value | 6 H | The key check value. |
| End Message Delimiter | 1 C | Present only if present in the command message. Value X'19. |
| Message Trailer | n A | Present only if present in the command message. Maximum length 32 characters. |