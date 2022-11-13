package com.pg85.otg.config.io;

import com.pg85.otg.config.ConfigFunction;
import com.pg85.otg.interfaces.ILogger;
import com.pg85.otg.interfaces.IMaterialReader;

import java.util.List;

public interface IConfigFunctionProvider {
    <T> ConfigFunction<T> getConfigFunction(String name, T holder, List<String> args, ILogger logger, IMaterialReader materialReader);
}
