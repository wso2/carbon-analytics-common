

            var getGadgetLocation = function (callback) {
            var gadgetLocation = "/portal/store/carbon.super/fs/gadget/temperature-count-chart";
            var PATH_SEPERATOR = "/";
            if (gadgetLocation.search("store") != -1) {
            wso2.gadgets.identity.getTenantDomain(function (tenantDomain) {
            var gadgetPath = gadgetLocation.split(PATH_SEPERATOR);
            var modifiedPath = '';
            for (var i = 1; i < gadgetPath.length; i++) {
            if (i === 3) {
            modifiedPath = modifiedPath.concat(PATH_SEPERATOR, tenantDomain);
            } else {
            modifiedPath = modifiedPath.concat(PATH_SEPERATOR, gadgetPath[i])
            }
            }
            callback(modifiedPath);
            });
            } else {
            callback(gadgetLocation);
            }
            }

        