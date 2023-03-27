package com.template.webserver


import com.template.flows.accounts.*
import com.template.flows.tokens.IssueForAccount
import com.template.flows.wallet.CreateWalletAccountInitiator
import com.template.utils.ResponseHandler
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.utilities.getOrThrow
import org.json.simple.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid


/**
 * Define your API endpoints here.
 */
@RestController

@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = ["/templateendpoint"], produces = ["text/plain"])
    private fun templateendpoint(): String {
        return "Define an endpoint here."
    }

    //Create acount
    @PostMapping(value = ["create-acc"], produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/x-www-form-urlencoded"])
    fun createAccount(request: HttpServletRequest): ResponseEntity<String> {
        val accName = request.getParameter("accName")

        return try {
            val signedTx = proxy.startTrackedFlow(::CreateNewAccount, accName).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("$signedTx")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    //Share acount
    @PostMapping(value = ["share-acc"], produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/x-www-form-urlencoded"])
    fun shareAccount(request: HttpServletRequest): ResponseEntity<String> {
        val accName = request.getParameter("accName")
        val recipient=request.getParameter("recipient")
        val partyX500Name = CordaX500Name.parse(recipient)
        val shareTo = proxy.wellKnownPartyFromX500Name(partyX500Name)
                ?: return ResponseEntity.badRequest().body("Party named $recipient cannot be found.\n")

        return try {
            val signedTx = proxy.startTrackedFlow(::ShareAccountTo, accName,shareTo).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("$signedTx")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }
    @GetMapping(value = ["my-accs"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMyACCs(): ResponseEntity<String?> {
        return try {
            val signedTx = proxy.startTrackedFlow(::ViewMyAccounts).returnValue.getOrThrow()

            ResponseEntity.status(HttpStatus.CREATED).body("${signedTx.toString()}")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    //Create acount
    @PostMapping(value = ["create-wallet"], produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/x-www-form-urlencoded"])
    fun createWalletAccount(  request: HttpServletRequest): ResponseEntity<Any> {

        val accountHolderName = request.getParameter("accountHolderName")
        val onlineAccountName = request.getParameter("onlineAccountName")
        val offlineAccountName = request.getParameter("offlineAccountName")
        val walletId = request.getParameter("walletId")

        val nullMessageObj=JSONObject()
        val badRequestStatus=HttpStatus.BAD_REQUEST

        if(accountHolderName.isNullOrEmpty()){
            nullMessageObj["message"] ="Account holder name cannot be null or empty"
            nullMessageObj["code"]=badRequestStatus
            return ResponseHandler.generateResponse(badRequestStatus,JSONObject(),nullMessageObj)
        }

        if(onlineAccountName.isNullOrEmpty()){
            nullMessageObj["message"] ="Online account name cannot be null or empty"
            nullMessageObj["code"]=badRequestStatus
            return ResponseHandler.generateResponse(badRequestStatus,JSONObject(),nullMessageObj)
        }

        if(offlineAccountName.isNullOrEmpty()){
            nullMessageObj["message"] ="Offline account name cannot be null or empty"
            nullMessageObj["code"]=badRequestStatus
            return ResponseHandler.generateResponse(badRequestStatus,JSONObject(),nullMessageObj)
        }

        if(walletId.isNullOrEmpty()){
            nullMessageObj["message"] ="Wallet ID cannot be null or empty"
            nullMessageObj["code"]=badRequestStatus
            return ResponseHandler.generateResponse(badRequestStatus,JSONObject(),nullMessageObj)
        }


        val obj = JSONObject()
        return try {
            val signedTx = proxy.startTrackedFlow(::CreateWalletAccountInitiator,onlineAccountName,offlineAccountName,walletId, accountHolderName).returnValue.getOrThrow()
            println(signedTx)
            val walletAddress=signedTx.walletAddress.id
            val accounts=signedTx.linkedAccounts
            val transactionDetails: MutableList<Any> = mutableListOf(walletId,accounts)

            obj["walletAddress"] = walletAddress
            obj["accounts"] = accounts

            ResponseHandler.generateResponse(HttpStatus.OK,obj,JSONObject())

        } catch (ex: Throwable) {

            logger.error(ex.message, ex)
            val ms=ex.message.toString()
            val errorMessage=ms.substring(ms.indexOf(":") + 1)
            obj["message"] =errorMessage
            obj["code"] =HttpStatus.BAD_REQUEST.value()
            ResponseHandler.generateResponse(HttpStatus.BAD_REQUEST,JSONObject(),obj)
//            ResponseEntity.badRequest().body(ex.message!!)
        }
    }



    @PostMapping(value = ["acc-issuance-token"], produces = [MediaType.TEXT_PLAIN_VALUE], headers = ["Content-Type=application/x-www-form-urlencoded"])
    fun issuanceTokenForAccount(request: HttpServletRequest): ResponseEntity<String> {

        val issuer = request.getParameter("issuer")
        val currency = request.getParameter("currency").toString()
        val holder = request.getParameter("holder").toString()
        val amount = request.getParameter("amount").toLong()
        val partyX500Name = CordaX500Name.parse(issuer)
        val otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name)
                ?: return ResponseEntity.badRequest().body("Party named $issuer cannot be found.\n")

        return try {
            val signedTx = proxy.startTrackedFlow(::IssueForAccount, amount, currency, otherParty,holder).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("$signedTx")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    //Get total tokens for the calling party
    @PostMapping(value = ["get-token-balance-acc"], produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/x-www-form-urlencoded"])
    fun getTokenBalanceForAccount(request: HttpServletRequest): ResponseEntity<String> {
        val accName = request.getParameter("accName").toString()

        return try {
            val signedTx = proxy.startTrackedFlow(::ViewBalanceForAccounts, accName).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("$signedTx")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }
//    @GetMapping(value = ["walletStates"], produces = [MediaType.APPLICATION_JSON_VALUE])
//    fun getIOUs() : ResponseEntity<List<AccountInformation>?> {
//        val test=proxy.vaultQueryBy<WalletState>().states
//        val transactionDetails: MutableList<AccountInformation> = mutableListOf()
//        test.forEach { states ->
//            states.state.data.linkedAccounts.forEach { obj-> transactionDetails.add(obj) }
//
//        }
//        val res=transactionDetails.toList()
//        return ResponseEntity.status(HttpStatus.CREATED).body(transactionDetails)
//    }transactionDetails


    @PostMapping(value = ["get-txn-history"], produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["Content-Type=application/x-www-form-urlencoded"])
    fun getTransactionHistory(request: HttpServletRequest): ResponseEntity<String> {
        val accName = request.getParameter("accName").toString()

        return try {
            val signedTx = proxy.startTrackedFlow(::GetTxnHistory, accName).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("$signedTx")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }
}