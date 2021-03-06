/*
 *       Copyright© (2018-2019) WeBank Co., Ltd.
 *
 *       This file is part of weidentity-build-tools.
 *
 *       weidentity-build-tools is free software: you can redistribute it and/or modify
 *       it under the terms of the GNU Lesser General Public License as published by
 *       the Free Software Foundation, either version 3 of the License, or
 *       (at your option) any later version.
 *
 *       weidentity-build-tools is distributed in the hope that it will be useful,
 *       but WITHOUT ANY WARRANTY; without even the implied warranty of
 *       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *       GNU Lesser General Public License for more details.
 *
 *       You should have received a copy of the GNU Lesser General Public License
 *       along with weidentity-build-tools.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.webank.weid.command;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.webank.weid.config.FiscoConfig;
import com.webank.weid.config.StaticConfig;
import com.webank.weid.constant.CnsType;
import com.webank.weid.constant.DataFrom;
import com.webank.weid.service.ConfigService;
import com.webank.weid.service.DeployService;
import com.webank.weid.util.FileUtils;

public class DeployContract extends StaticConfig {
    
    /**
     * log4j.
     */
    private static final Logger logger = LoggerFactory.getLogger(DeployContract.class);
    
    private static DeployService deployService = new DeployService();
    private static ConfigService configService = new ConfigService();
    
    public static void main(String[] args) {
        logger.info("[DeployContract] execute contract deployment.");
        
        CommandArgs commandArgs = new CommandArgs();
        JCommander.newBuilder()
            .addObject(commandArgs)
            .build()
            .parse(args);
        String privateKeyFile = commandArgs.getPrivateKey();
        if (StringUtils.isNotBlank(privateKeyFile)) {
            String privateKey = FileUtils.readFile(privateKeyFile);
            deployService.createAdmin(privateKey);
        }
        
        FiscoConfig fiscoConfig = configService.loadNewFiscoConfig();
        String hash = deployService.deploy(fiscoConfig, DataFrom.COMMAND);
        System.out.println("the contract deploy successfully  --> hash : " +  hash);
       //配置启用新hash
        configService.enableHash(hash);
        System.out.println("begin enable the hash.");
        //节点启用新hash并停用原hash
        deployService.enableHash(CnsType.DEFAULT, hash, fiscoConfig.getCnsContractFollow());
        //重新加载合约地址
        configService.reloadAddress();
        System.out.println("begin create the weId for admin and deploy the systemCpt.");
        deployService.deploySystemCpt(hash, DataFrom.COMMAND);
        System.out.println("the systemCpt deploy successfully.");
        System.exit(0);
    }

}
