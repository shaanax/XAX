/*
 * Copyright © 2013-2016 The XAX Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the XAX software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package xax.http;

import xax.Attachment;
import xax.XAX;
import xax.XAXException;
import xax.Transaction;
import xax.TransactionType;
import xax.util.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public final class GetExpectedAssetDeletes extends APIServlet.APIRequestHandler {

    static final GetExpectedAssetDeletes instance = new GetExpectedAssetDeletes();

    private GetExpectedAssetDeletes() {
        super(new APITag[] {APITag.AE}, "asset", "account", "includeAssetInfo");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws XAXException {

        long assetId = ParameterParser.getUnsignedLong(req, "asset", false);
        long accountId = ParameterParser.getAccountId(req, "account", false);
        boolean includeAssetInfo = "true".equalsIgnoreCase(req.getParameter("includeAssetInfo"));

        Filter<Transaction> filter = transaction -> {
            if (transaction.getType() != TransactionType.ColoredCoins.ASSET_DELETE) {
                return false;
            }
            if (accountId != 0 && transaction.getSenderId() != accountId) {
                return false;
            }
            Attachment.ColoredCoinsAssetDelete attachment = (Attachment.ColoredCoinsAssetDelete)transaction.getAttachment();
            return assetId == 0 || attachment.getAssetId() == assetId;
        };

        List<? extends Transaction> transactions = XAX.getBlockchain().getExpectedTransactions(filter);

        JSONObject response = new JSONObject();
        JSONArray deletesData = new JSONArray();
        transactions.forEach(transaction -> deletesData.add(JSONData.expectedAssetDelete(transaction, includeAssetInfo)));
        response.put("deletes", deletesData);

        return response;
    }

}
