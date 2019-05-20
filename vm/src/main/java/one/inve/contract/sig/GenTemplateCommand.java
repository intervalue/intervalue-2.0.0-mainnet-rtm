package one.inve.contract.sig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import one.inve.contract.sig.struct.ContractEnforcement;
import one.inve.contract.sig.struct.ContractTerms;
import one.inve.contract.tuple.Tuple;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: generate a contract signature configuration template filled
 *               with some data
 * @author: Francis.Deng
 * @date: 2018年12月11日 下午7:29:15
 * @version: V1.0
 */
public class GenTemplateCommand implements ICommand {

	/**
	 * "template location" && "template file name" are necessary parameters
	 */
	@Override
	public Optional<Tuple> execute(String... actions) {
		File f = new File(actions[0] + File.separator + actions[1]);

		try (FileWriter f2 = new FileWriter(f, false)) {
			f2.write(toJson(newContractEnforcement()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return Optional.empty();// neglect result;

	}

	protected ContractEnforcement newContractEnforcement() {
		ContractTerms terms = new ContractTerms();

		terms.setCalldata("");
		terms.setToAddress("");
		terms.setSender("4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7");
		terms.setGasPrice("1000000000");
		terms.setValue("0");
		terms.setGasLimit("2000000");
		terms.setMnemonicCode("shield salmon sport horse cool hole pool panda embark wrap fancy equip");

		ContractEnforcement enforcement = new ContractEnforcement();
		enforcement.setUnit(terms);
		enforcement.setTarget("");
		enforcement.setMethod("");

		return enforcement;
	}

	protected String toJson(ContractEnforcement enforcement) {
		return JSON.toJSONString(enforcement, SerializerFeature.PrettyFormat);
	}

}
