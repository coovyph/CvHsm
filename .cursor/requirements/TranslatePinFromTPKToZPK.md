## Translate PIN from TPK to ZPK

### Request message to HSM
| Field | Length & Type | Details |
| --- | --- | ---|
| Message Header | m A | Subsequently returned to Host unchanged
| Command Code | 2 A | Value 'CA' |
| Source TPK | 16 H or  'U' + 32 H   or 'T' + 48 H | TPK value under variant |
| Destination ZPK | 16 H or  'U' + 32 H   or 'T' + 48 H | ZPK value under variant |
| Minimum PIN Length | 2 N | Valid range is '04' to '12' |
| Source PIN Block  | 16 H or 32 H | The source pin block encrypted under source TPK |
| Source PIN Block Format | 2 N | the format code of source pin block |
| Destination PIN Block Format | 2 N | the format code of destination pin block |
| PAN/Token | n N | If 'Source PIN Block' uses a token instead of the actual PAN, this field will contain the token number. Otherwise, it will contain the real PAN.  |

### Response message from HSM
| Field | Length & Type | Details |
| --- | --- | ---|
| Message Header | m A | Subsequently returned to Host unchanged
| Command Code | 2 A | Value 'CB' |
| Error Code | 2 A | '00': No error<br>'10': Source TPK parity error<br>'11': Destination ZPK parity error<br>'17': PIN token translation disabled<br>'68': Command disabled<br>'69': PIN Block format has been disabled  |
| PIN Length | 2 N | Return the length of the pin |
| Destination PIN Block | 16 H or <br> 32 H | The destination PIN block encrypted under the Destination ZPK.<br>When using a DES Destination ZPK, this field will be 16 H.<br>When using an AES Destination ZPK, this field will be 32 H. |
| Source PIN Block Format | 2 N | the format code of source pin block |
