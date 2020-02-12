package one.inve.localfullnode2.utilities;

import java.math.BigInteger;

import org.junit.Test;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: SignerTest
 * @Description: act as a demo or tool to output a signed message and a signed
 *               escaped message which is used by post tools like postman
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Feb 11, 2020
 *
 */
public class SignerTest {
	@Test
	public void testOutputSignedMessage() {
//		// {"address":"FZILFV3M4ONPGZYNN4Y6VMODIMBPNSCY","extKeys":{"privKey":"8/reNSHrNkUqWlpqdDxPCdE/tzk4sItCHU5OZfJE0/A=","pubKey":"A6Hlm/qtJ3tGXPMpsdfDnyr7Xijv3yPT7RwdtJovG0xl"},
//		// "keys":{"privKey":"+gcIpmJeOjJd8VT21OXc/DxI3/0hm63ZKMbIFfJKoiI=","pubKey":"A8dBZToFtboCcq4ltE3gm+N9wkLPOagtJ1AGbTxSx/M/"},"mnemonic":"valve
//		// expose mention hire cart work midnight valley weapon pigeon hobby brave"}
//		Signer signer = new Signer("A6Hlm/qtJ3tGXPMpsdfDnyr7Xijv3yPT7RwdtJovG0xl",
//				"8/reNSHrNkUqWlpqdDxPCdE/tzk4sItCHU5OZfJE0/A=");
//
//		String tx = signer.signTx("FZILFV3M4ONPGZYNN4Y6VMODIMBPNSCY", "AJXW6OZSFDM7IWZ2HAQM77ZFFC2N2EGO",
//				BigInteger.valueOf(1000000), BigInteger.valueOf(500000), BigInteger.valueOf(1000000000));

		String tx = genSignedM0();
		System.out.println("signed message:");
		System.out.println(tx);

	}

	@Test
	public void testOutputPostingMessage() {
		String signed = genSignedM0();
		String escaped = signed.replaceAll("\"", "\\\\\"");
		String postingMessage = String.format("{\"message\":\"%s\"}", escaped);
		System.out.println("escaped message:");
		System.out.println(postingMessage);

	}

	private String genSignedM0() {
		// {"address":"FZILFV3M4ONPGZYNN4Y6VMODIMBPNSCY","extKeys":{"privKey":"8/reNSHrNkUqWlpqdDxPCdE/tzk4sItCHU5OZfJE0/A=","pubKey":"A6Hlm/qtJ3tGXPMpsdfDnyr7Xijv3yPT7RwdtJovG0xl"},
		// "keys":{"privKey":"+gcIpmJeOjJd8VT21OXc/DxI3/0hm63ZKMbIFfJKoiI=","pubKey":"A8dBZToFtboCcq4ltE3gm+N9wkLPOagtJ1AGbTxSx/M/"},"mnemonic":"valve
		// expose mention hire cart work midnight valley weapon pigeon hobby brave"}
		Signer signer = new Signer("A6Hlm/qtJ3tGXPMpsdfDnyr7Xijv3yPT7RwdtJovG0xl",
				"8/reNSHrNkUqWlpqdDxPCdE/tzk4sItCHU5OZfJE0/A=");

		String tx = signer.signTx("FZILFV3M4ONPGZYNN4Y6VMODIMBPNSCY", "AJXW6OZSFDM7IWZ2HAQM77ZFFC2N2EGO",
				BigInteger.valueOf(1000000), BigInteger.valueOf(500000), BigInteger.valueOf(1000000000));

		return tx;
	}

	// Genesis information
//	{"address":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","extKeys":{"privKey":"gmVRv5VeCGFdTZMdSOIlwjrLZjwERuQb7DRcCiolG50=","pubKey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"},"keys":{"privKey":"kiXaGNIR5ktt90IAhNXt/xcOedOiQP0tZGdNZFnP7ys=","pubKey":"AioOUX+JS5IoW5P9VEb7z3E+Yg+2MhOw1D/NXjGudFtK"},"mnemonic":"play present amateur federal safe goddess tissue glide boat expire kiss edit"}
//
//
//	{"address":"FZILFV3M4ONPGZYNN4Y6VMODIMBPNSCY","extKeys":{"privKey":"8/reNSHrNkUqWlpqdDxPCdE/tzk4sItCHU5OZfJE0/A=","pubKey":"A6Hlm/qtJ3tGXPMpsdfDnyr7Xijv3yPT7RwdtJovG0xl"},"keys":{"privKey":"+gcIpmJeOjJd8VT21OXc/DxI3/0hm63ZKMbIFfJKoiI=","pubKey":"A8dBZToFtboCcq4ltE3gm+N9wkLPOagtJ1AGbTxSx/M/"},"mnemonic":"valve expose mention hire cart work midnight valley weapon pigeon hobby brave"}
//	{"address":"6THIEWBSSC3ZWDJSK57KQSHBEO5NQFGS","extKeys":{"privKey":"n2OLBx2eTlw3lvCeLb15a13Ybv30XDrtGFyLPktnMcQ=","pubKey":"A41bUZIFEIFwu16k9jdufFIo+qXkZ2ogsHoFMfH9Y8Ig"},"keys":{"privKey":"wqb3fsNebGmmle6s4VtiqiHlIfQsHQb6vUdQno5cP44=","pubKey":"Aztw/LgANwtUJiAOvbQi+ZjSqd7a9ROQ9a0H8UpcvEbV"},"mnemonic":"catch clip evil damp gallery legal token fuel chaos relax demand math"}
//	{"address":"3DYR2LRFU4AY4NV52VPGS3ASJZYA7XEE","extKeys":{"privKey":"oy085kfJUvOnZtAO/jqU5FrYPhBg4Yf/9qn7KIPbNf4=","pubKey":"AoI6IqGNey0/tghjUvGBfKfQAwxjI3EAbCp2MHqICTnn"},"keys":{"privKey":"9+uaLi8eMZcKLWISGAyJFny4EaeUTC/9611pRWrUHQI=","pubKey":"Ag8uJgU0QRowuLjdTjdQ0fBEP2JUdPrAGV+E9FLspFth"},"mnemonic":"cool frown moment hidden polar reveal expect error miracle alone silk crunch"}
//	{"address":"2HUMFDOSIMUJD33BNAYDOBN3OBJRWQHL","extKeys":{"privKey":"Ezzm8KnVx2WGJYH3JPd3mUhPs8w76oj6EamP27iujrY=","pubKey":"Apno/eTbA/lDVT/Gf6PtxI9COvTRla/IiGTU7rneY+jo"},"keys":{"privKey":"vA6cNylJT/ENf2mHoRlb8ygnALWdVyRJoImPtAWmjnI=","pubKey":"A41a9ZWK1T437fm/eUq0meLTW+1CYtlOjAWWY0N3pAkO"},"mnemonic":"rigid crack spend very useless shop hold pepper enhance piece denial law"}
//	{"address":"HSLOIY5A3AYCOSWUUWFJLFJVSNZBWWYF","extKeys":{"privKey":"VJ3U2LOUfOGGh0ChROMwSYNrLMoGCoM+JrLBqzcwguQ=","pubKey":"AjT3S9NY5fGIhbJogLmb17BUuHSM/YOczcaUJpfnWYLC"},"keys":{"privKey":"G/J+4+bV8y6HdQQP0v6nbCdLEz8hZfJDOAJUuQrivFM=","pubKey":"A4ksZxmbBpLIxu3KXatnZyOnR2qRlI3nE1a61wGWuvAk"},"mnemonic":"episode retire fire stick palm mercy riot police trial question future walk"}
//	{"address":"HRXZU43F52CMOYPB2TTY5CAK3WWOX5F6","extKeys":{"privKey":"tJn1G+E3dZvusMLCM6WlyUwYI7Sn/VXH+qX9VpWZM0I=","pubKey":"ArLimjSk6qOxe7IXyhyc6vpb0Z6iBi4WA71zhDGh5mm/"},"keys":{"privKey":"QRDyzzB6ECjHcZZo0NCROewgj3Ih6naH7NyY0r7opzY=","pubKey":"Az8/knB/69MAqxIQ/JMZCoDhdEff4T13XtbIEezF0NLI"},"mnemonic":"bottom kingdom comfort forest fiber piano whisper museum review puppy scrub frown"}
//	{"address":"G3IDLG6BP2VQOERXDYJFR5I2ZZTPXTZX","extKeys":{"privKey":"7xy1mWqQaFjSIAqPJsrb8T2wTqQRUoHUIrolPn3PhAE=","pubKey":"A7Hkosfj/RoxIuTrwPKgxqxf+VE6cAjC16pyT5ydSh9b"},"keys":{"privKey":"Eo/qOuALAm1W5OmvrUKYVD2SaARIMq5LYeJ+WkI/QTg=","pubKey":"Aoa3ip3hr6jrDBIkJqKhdoIELNQw8WAybWQeEMD4688X"},"mnemonic":"volume pigeon hood slow tackle peanut mystery attend wild liquid tail muscle"}
//	{"address":"7EE56Q6KHRDHA6RKANHJW2LQCQZP62SE","extKeys":{"privKey":"Qwdq9TpgAusY8eFqcmACeYt47/xMMSBTcODmAzfhIvw=","pubKey":"At4oZBeBoIP0YJEn4cmGRTLJBCgFjK8fa6S+lA/pcjuA"},"keys":{"privKey":"P9SAMN/17fNVGYLFHSqfkt6WjItHdsSGkuR1TmMmtoc=","pubKey":"AtdehhZSKoDL0xqUPU/iS9oAHkLxS/qwctIzSrlkvRds"},"mnemonic":"rent found intact excuse track blur attitude pull three pudding woman orient"}
//	{"address":"4NUALQ3KNOODUKU3XUUJ7KWD5H6WC7FQ","extKeys":{"privKey":"2KOdbtt1UgC8WkkR74mnJfe20RXRK+4u34yAh57MS7E=","pubKey":"A/OiqT0Gb+ocgbcUHaLefbHqVgXTyMGWxkA8E6zhyl2e"},"keys":{"privKey":"eVvkiy5RYAfImZQZEDgW8O7DrAs0znXT5kdtmL98Nts=","pubKey":"A1L8CG/lG+Mr3JOv6qzNFPD0aA0LpfMGvdvKapjULSzl"},"mnemonic":"total crystal vivid combine engine grape pumpkin giraffe dose cushion craft employ"}
//	{"address":"AJXW6OZSFDM7IWZ2HAQM77ZFFC2N2EGO","extKeys":{"privKey":"I4cEwppm/+lpSzHOrx4Vf1C3KdQBvesF/FLZa6cGwvk=","pubKey":"AyL+CP/RLme7+LhqgDVvLxBncBagJ1oOhjJQq5NqA0bc"},"keys":{"privKey":"d723PxdDOcEfNvz33Z3ZSiTXsznIwX83HC2nHbxI9sQ=","pubKey":"AlUDwuARnpT0soSOZkIamtXoY6VcYQKp0EZ0ZQQtWmwn"},"mnemonic":"key crane faint shed round body police resource snap toe square sting"}
//
//
//	{"message":{"nrgPrice":"1000000000","amount":"1027448266000000000000000000","signature":"33AOI4qkU1JEEOPiO8PnaD+5T9isZXhsRGhdUXyFe1r2ZnEYXBXLVrvSAWM0gOiY+Iu4BHIFhK2vqvKsUkAIWgjXE=","fee":"500000","vers":"2.0","fromAddress":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","remark":"","type":1,"toAddress":"FZILFV3M4ONPGZYNN4Y6VMODIMBPNSCY","timestamp":1579681767024,"pubkey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"}}
//	{"message":{"nrgPrice":"1000000000","amount":"1795577995000000000000000000","signature":"32DcohiOYK4jh7Br7OTfDsgKA9WEX244CEySVfmfSNuNJ/hsmybjo2tJx80kgbNZhkDMApCHWY/oDPquSABglM5w==","fee":"500000","vers":"2.0","fromAddress":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","remark":"","type":1,"toAddress":"6THIEWBSSC3ZWDJSK57KQSHBEO5NQFGS","timestamp":1579681767218,"pubkey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"}}
//	{"message":{"nrgPrice":"1000000000","amount":"1232099984000000000000000000","signature":"32RgyFx+VETC6PrUoy7c+08N9nbNpG54xA44e5SySHC4oRi/lIXlZi7N9TOMo+nw8MKBNQ4Xfgp7aXoahtesAnWA==","fee":"500000","vers":"2.0","fromAddress":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","remark":"","type":1,"toAddress":"3DYR2LRFU4AY4NV52VPGS3ASJZYA7XEE","timestamp":1579681767298,"pubkey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"}}
//	{"message":{"nrgPrice":"1000000000","amount":"775404936000000000000000000","signature":"32AvRrf9cdDf0M1eRmRpdVjCnA3MhnkF02ELER6asnWLh9/D8WgDDc2hFJQFNgcQs/M5bYvHISE1fsMSmQlLUX4Q==","fee":"500000","vers":"2.0","fromAddress":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","remark":"","type":1,"toAddress":"2HUMFDOSIMUJD33BNAYDOBN3OBJRWQHL","timestamp":1579681767370,"pubkey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"}}
//	{"message":{"nrgPrice":"1000000000","amount":"1496793396000000000000000000","signature":"33ALJFoP+eY2gHcGyjCsranXM7YMZ58P+4nzcv4P38amJoA7KlaJOWWKu0wq0yfMdMJ7p+Z6AzzeoAK1UqnVGA8zs=","fee":"500000","vers":"2.0","fromAddress":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","remark":"","type":1,"toAddress":"HSLOIY5A3AYCOSWUUWFJLFJVSNZBWWYF","timestamp":1579681767451,"pubkey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"}}
//	{"message":{"nrgPrice":"1000000000","amount":"1052163538000000000000000000","signature":"32LQFE4HNPesdqyDR4ayyvFoD8PiG27YFVHcCJw2Y0g7FI6ekSjOi2+A5QY2tCIsEuRzVWJ6ZngbRzT9gSXWKDOQ==","fee":"500000","vers":"2.0","fromAddress":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","remark":"","type":1,"toAddress":"HRXZU43F52CMOYPB2TTY5CAK3WWOX5F6","timestamp":1579681767511,"pubkey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"}}
//	{"message":{"nrgPrice":"1000000000","amount":"702027144000000000000000000","signature":"32SjG3LPPJ2wTug4EQ3O2KlJOADiwaIxivfNvamYH/iqJokVoGaPlOfbJVLxDSTEO4AdUOgtbthw2T89QIstcc8g==","fee":"500000","vers":"2.0","fromAddress":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","remark":"","type":1,"toAddress":"G3IDLG6BP2VQOERXDYJFR5I2ZZTPXTZX","timestamp":1579681767549,"pubkey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"}}
//	{"message":{"nrgPrice":"1000000000","amount":"185064890000000000000000000","signature":"32O8mRNHUT1Op+6vJxyYm0sv8yT95c+/49oa+hxWJsCu0HzsyaEUnSWRTzhaGdOrOyDvbOs9zQJqRLFfglUM7lbA==","fee":"500000","vers":"2.0","fromAddress":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","remark":"","type":1,"toAddress":"7EE56Q6KHRDHA6RKANHJW2LQCQZP62SE","timestamp":1579681767584,"pubkey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"}}
//	{"message":{"nrgPrice":"1000000000","amount":"654996200000000000000000000","signature":"32CTmzMzqbf8nXWUIWl7Z4tKMRk0SNmlXTlNpOXyWVVpVXc1xzdjQwwQpyr7u7FDyTUKMfxzhQKT8ibA8IO3u9Lg==","fee":"500000","vers":"2.0","fromAddress":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","remark":"","type":1,"toAddress":"4NUALQ3KNOODUKU3XUUJ7KWD5H6WC7FQ","timestamp":1579681767621,"pubkey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"}}
//	{"message":{"nrgPrice":"1000000000","amount":"1078423651000000000000000000","signature":"33ANfz+RvCqGkDejTBGBd7b3zznEkU9N5Vo+R/r1ZQwEHTBJxqECvHZs3evzkd4YUejGEmjY/bP+IasPXgmZZWua0=","fee":"500000","vers":"2.0","fromAddress":"MQ5C6CIJBB2YYNM2MA7X6B2MCKHGZN4H","remark":"","type":1,"toAddress":"AJXW6OZSFDM7IWZ2HAQM77ZFFC2N2EGO","timestamp":1579681767690,"pubkey":"A7oZedrYesHVMYsUJsnachl/3DiLrrk1Xrw9Aove4D+J"}}	
}
