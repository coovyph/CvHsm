## COMMAND MESSAGE

| Field | Length & Type | Details |
|---|---|---|
| Message Header | m A | Subsequently returned to the Host unchanged. |
| Command Code | 2 A | Value 'A8'. |
| Key Type | 3 H | **For a Variant LMK:**<br>Indicates the key type of the key to be exported. For a list of possible values, see *Available Key Types/Usages* (p.37).<br><br>**For a Key Block LMK:**<br>This field is ignored; should be set to 'FFF'. |
| Delimiter | 1 A | Value ';'. Optional.<br>If present, the following field must be present. |
| ZMK/TMK Flag | 1 N | Optional. Only present if the above delimiter is present.<br>'0': ZMK (default value if these fields are not present)<br>'1': TMK |
| ZMK (or TMK) | | The Zone Master Key (or Terminal Master Key). |
| | 16 H<br>or<br>32 H<br>or<br>'U' + 32 H<br>or<br>'T' + 48 H | **For a Variant LMK,** the 'ZMK' is encrypted under LMK pair 04-05 variant 0. The 'TMK' is encrypted under LMK pair 14-15 variant 0 if the security setting "Enforce key type 002 separation for PCI HSM compliance" has the value "N" or LMK pair 36-37 variant 8 if the setting has the value "Y"). |
| | or<br>'S' + n A | **For a Key Block LMK,** the 'ZMK (or TMK)' must comply with the following:<br>\| Key Usage \| Algorithm \| Mode of Use \|<br>\|---|---|---\|<br>\| 'K0', '51', '52' \| 'D', 'T', 'A' \| 'B', 'E', 'N' \| |
| Key | | Key to be exported; encrypted under the LMK. |
| | 16 H<br>or<br>'U' + 32 H<br>or<br>'T' + 48 H | **For a Variant LMK,** the 'Key' is encrypted under the LMK pair/variant indicated by the Key Type.<br>For export to Verifone/GISKE key block format, the 'Key' must be a 3DES TPK or TAK. |
| | or<br>'S' + n A | **For a Key Block LMK,** the 'Key' must comply with the following:<br>\| Key Usage \| Algorithm \| Mode of Use \|<br>\|---|---|---\|<br>\| Any valid value \| 'D', 'T', 'A' \| Any valid value \|<br>For a list of valid Key Usage values, refer to *Available Key Types/Usages* (p. 37).<br>For export to Verifone/GISKE key block format, the 'Key' must comply with the following:<br>\| Key Usage \| Algorithm \| Mode of Use \|<br>\|---|---|---\|<br>\| 'M0', 'M3', 'P0', '71' \| 'T' \| Any valid value \| |
| Key Scheme (ZMK or TMK) | 1 A | Indicates the scheme for encrypting the key under the ZMK (or TMK). For a list of key schemes, see the Key Scheme Table at Appendix A of the *payShield 10K Host Programmer's Manual*. |
| Atalla Variant | 1 / 2 N | Optional. Atalla variant; for use in systems with Atalla equipment. |
| Delimiter | 1 A | Value '%'. Optional; if present, the following field must be present. |
| LMK Identifier | 2 N | LMK identifier; min value = '00'; max value is defined by license; must be present if the above Delimiter is present. |

The following section applies when exporting to a key block format (e.g. TR-31 or Thales Key Block) when using a Key Block LMK.

| Field | Length & Type | Details |
|---|---|---|
| Delimiter | 1 A | Value '&'. Optional; can only be present when the exported key is in key block format; if present, the following fields must be present. |
| Modified Exportability | 1 A | Exportability field, to be included in the key block header; only permitted value is "N"; must be present if the above Delimiter is present. |
| Delimiter | 1 A | Value '!'. Optional; can only be present when exporting the key to TR-31 key block format. If present, the following field must be present. |
| Key Block Version ID | 1 A | Key Block Version ID<br>'A' – Key block protected using the Key Variant Binding Method (see Ref 8 section 5.3.3.1)<br>'B' – Key block protected using the Key Derivation Binding Method (see Ref 8 section 5.3.2.1)<br>'C' – Key block protected using the Key Variant Binding Method (see Ref 8 section 5.3.3.1)<br>'D' – Key block protected using the AES Key Derivation Binding Method (see Ref 8 section 5.3.2.3). |

---

## RESPONSE MESSAGE

| Field | Length & Type | Details |
|---|---|---|
| Message Header | m A | Returned to the Host unchanged. |
| Response Code | 2 A | Value 'A9'. |
| Error Code | 2 A | '00': No error<br>'10': ZMK or TMK Parity error<br>'11': Key parity error<br>'68': Command disabled<br>or a standard error code. |
| Key (under ZMK or TMK) | 16 H<br>or<br>'U' + 32 H<br>or<br>'T' + 48 H<br>or<br>'R' + n A<br>or<br>'S' + n A<br>or<br>'V' + n A | The key encrypted under ZMK or TMK. |
| Key Check Value | 6 H | The key check value. |
| End Message Delimiter | 1 C | Present only if present in the command message. Value X'19. |
| Message Trailer | n A | Present only if present in the command message. Maximum length 32 characters. |
