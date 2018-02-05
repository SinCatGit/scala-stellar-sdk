package stellar.sdk.resp

import org.json4s.JsonAST.JObject
import org.json4s.{CustomSerializer, DefaultFormats}
import stellar.sdk._

sealed trait EffectResp {
  val id: String
}

case class EffectAccountCreated(id: String, account: PublicKeyOps, startingBalance: NativeAmount) extends EffectResp
case class EffectAccountDebited(id: String, account: PublicKeyOps, amount: Amount) extends EffectResp

class EffectRespDeserializer extends CustomSerializer[EffectResp](format => ({
  case o: JObject =>
    implicit val formats = DefaultFormats
    def account = KeyPair.fromAccountId((o \ "account").extract[String])
    def amount = {
      val units = Amount.toBaseUnits((o \ "amount").extract[String].toDouble).get
      def assetCode = (o \ "asset_code").extract[String]
      def assetIssuer = KeyPair.fromAccountId((o \ "asset_issuer").extract[String])
      (o \ "asset_type").extract[String] match {
        case "native" => NativeAmount(units)
        case "credit_alphanum4" => IssuedAmount(units, AssetTypeCreditAlphaNum4(assetCode, assetIssuer))
        case "credit_alphanum12" => IssuedAmount(units, AssetTypeCreditAlphaNum12(assetCode, assetIssuer))
        case t => throw new RuntimeException(s"Unrecognised asset type '$t'")
      }
    }
    val id = (o \ "id").extract[String]
    (o \ "type").extract[String] match {
      case "account_created" =>
        val startingBalance = Amount.lumens((o \ "starting_balance").extract[String].toDouble).get
        EffectAccountCreated(id, account, startingBalance)
      case "account_debited" =>
        EffectAccountDebited(id, account, amount)
      case t => throw new RuntimeException(s"Unrecognised effect type '$t'")
    }
}, PartialFunction.empty)
)