package com.iota.iri.benchmarks.crypto;

import com.iota.iri.crypto.Curl;
import com.iota.iri.crypto.SpongeFactory;
import com.iota.iri.utils.Converter;
import com.iota.iri.utils.Pair;
import org.junit.Assert;
import org.openjdk.jmh.annotations.Benchmark;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CurlBenchmark {
  private final static String TRYTES = "RSWWSFXPQJUBJROQBRQZWZXZJWMUBVIVMHPPTYSNW9YQIQQF9RCSJJCVZG9ZWITXNCSBBDHEEKDRBHVTWCZ9SZOOZHVBPCQNPKTWFNZAWGCZ9QDIMKRVINMIRZBPKRKQAIPGOHBTHTGYXTBJLSURDSPEOJ9UKJECUKCCPVIQQHDUYKVKISCEIEGVOQWRBAYXWGSJUTEVG9RPQLPTKYCRAJ9YNCUMDVDYDQCKRJOAPXCSUDAJGETALJINHEVNAARIPONBWXUOQUFGNOCUSSLYWKOZMZUKLNITZIFXFWQAYVJCVMDTRSHORGNSTKX9Z9DLWNHZSMNOYTU9AUCGYBVIITEPEKIXBCOFCMQPBGXYJKSHPXNUKFTXIJVYRFILAVXEWTUICZCYYPCEHNTK9SLGVL9RLAMYTAEPONCBHDXSEQZOXO9XCFUCPPMKEBR9IEJGQOPPILHFXHMIULJYXZJASQEGCQDVYFOM9ETXAGVMSCHHQLFPATWOSMZIDL9AHMSDCE9UENACG9OVFAEIPPQYBCLXDMXXA9UBJFQQBCYKETPNKHNOUKCSSYLWZDLKUARXNVKKKHNRBVSTVKQCZL9RY9BDTDTPUTFUBGRMSTOTXLWUHDMSGYRDSZLIPGQXIDMNCNBOAOI9WFUCXSRLJFIVTIPIAZUK9EDUJJ9B9YCJEZQQELLHVCWDNRH9FUXDGZRGOVXGOKORTCQQA9JXNROLETYCNLRMBGXBL9DQKMOAZCBJGWLNJLGRSTYBKLGFVRUF9QOPZVQFGMDJA9TBVGFJDBAHEVOLW9GNU9NICLCQJBOAJBAHHBZJGOFUCQMBGYQLCWNKSZPPBQMSJTJLM9GXOZHTNDLGIRCSIJAZTENQVQDHFSOQM9WVNWQQJNOPZMEISSCLOADMRNWALBBSLSWNCTOSNHNLWZBVCFIOGFPCPRKQSRGKFXGTWUSCPZSKQNLQJGKDLOXSBJMEHQPDZGSENUKWAHRNONDTBLHNAKGLOMCFYRCGMDOVANPFHMQRFCZIQHCGVORJJNYMTORDKPJPLA9LWAKAWXLIFEVLKHRKCDG9QPQCPGVKIVBENQJTJGZKFTNZHIMQISVBNLHAYSSVJKTIELGTETKPVRQXNAPWOBGQGFRMMK9UQDWJHSQMYQQTCBMVQKUVGJEAGTEQDN9TCRRAZHDPSPIYVNKPGJSJZASZQBM9WXEDWGAOQPPZFLAMZLEZGXPYSOJRWL9ZH9NOJTUKXNTCRRDO9GKULXBAVDRIZBOKJYVJUSHIX9F9O9ACYCAHUKBIEPVZWVJAJGSDQNZNWLIWVSKFJUMOYDMVUFLUXT9CEQEVRFBJVPCTJQCORM9JHLYFSMUVMFDXZFNCUFZZIKREIUIHUSHRPPOUKGFKWX9COXBAZMQBBFRFIBGEAVKBWKNTBMLPHLOUYOXPIQIZQWGOVUWQABTJT9ZZPNBABQFYRCQLXDHDEX9PULVTCQLWPTJLRSVZQEEYVBVY9KCNEZXQLEGADSTJBYOXEVGVTUFKNCNWMEDKDUMTKCMRPGKDCCBDHDVVSMPOPUBZOMZTXJSQNVVGXNPPBVSBL9WWXWQNMHRMQFEQYKWNCSW9URI9FYPT9UZMAFMMGUKFYTWPCQKVJ9DIHRJFMXRZUGI9TMTFUQHGXNBITDSORZORQIAMKY9VRYKLEHNRNFSEFBHF9KXIQAEZEJNQOENJVMWLMHI9GNZPXYUIFAJIVCLAGKUZIKTJKGNQVTXJORWIQDHUPBBPPYOUPFAABBVMMYATXERQHPECDVYGWDGXFJKOMOBXKRZD9MCQ9LGDGGGMYGUAFGMQTUHZOAPLKPNPCIKUNEMQIZOCM9COAOMZSJ9GVWZBZYXMCNALENZ9PRYMHENPWGKX9ULUIGJUJRKFJPBTTHCRZQKEAHT9DC9GSWQEGDTZFHACZMLFYDVOWZADBNMEM9XXEOMHCNJMDSUAJRQTBUWKJF9RZHK9ACGUNI9URFIHLXBXCEODONPXBSCWP9WNAEYNALKQHGULUQGAFL9LB9NBLLCACLQFGQMXRHGBTMI9YKAJKVELRWWKJAPKMSYMJTDYMZ9PJEEYIRXRMMFLRSFSHIXUL9NEJABLRUGHJFL9RASMSKOI9VCFRZ9GWTMODUUESIJBHWWHZYCLDENBFSJQPIOYC9MBGOOXSWEMLVU9L9WJXKZKVDBDMFSVHHISSSNILUMWULMVMESQUIHDGBDXROXGH9MTNFSLWJZRAPOKKRGXAAQBFPYPAAXLSTMNSNDTTJQSDQORNJS9BBGQ9KQJZYPAQ9JYQZJ9B9KQDAXUACZWRUNGMBOQLQZUHFNCKVQGORRZGAHES9PWJUKZWUJSBMNZFILBNBQQKLXITCTQDDBV9UDAOQOUPWMXTXWFWVMCXIXLRMRWMAYYQJPCEAAOFEOGZQMEDAGYGCTKUJBS9AGEXJAFHWWDZRYEN9DN9HVCMLFURISLYSWKXHJKXMHUWZXUQARMYPGKRKQMHVR9JEYXJRPNZINYNCGZHHUNHBAIJHLYZIZGGIDFWVNXZQADLEDJFTIUTQWCQSX9QNGUZXGXJYUUTFSZPQKXBA9DFRQRLTLUJENKESDGTZRGRSLTNYTITXRXRGVLWBTEWPJXZYLGHLQBAVYVOSABIVTQYQM9FIQKCBRRUEMVVTMERLWOK";
  private final static String HASH = "TIXEPIEYMGURTQ9ABVYVQSWMNGCVQFASMFAEQWUZCLIWLCDIGYVXOEJBBEMZOIHAYSUQMEFOGZBXUMHQW";

  /**
   * Benchmark absorb and squeeze methods of Curl 81 hash function.
   */
  @Benchmark
  public void curl() {
    int size = 8019;
    byte[] inTrits = new byte[size];
    byte[] hashTrits = new byte[Curl.HASH_LENGTH];
    Converter.trits(TRYTES, inTrits, 0);
    Curl curl = (Curl) SpongeFactory.create(SpongeFactory.Mode.CURLP81);
    curl.absorb(inTrits, 0, inTrits.length);
    curl.squeeze(hashTrits, 0, Curl.HASH_LENGTH);
    String outTrytes = Converter.trytes(hashTrits);
    Assert.assertEquals(HASH, outTrytes);
  }

  /**
   * Benchmark absorb and squeeze methods of pair Curl 81 hash function.
   */
  @Benchmark
  public void pairCurl() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    int size = 8019;
    byte[] inTrits = new byte[size];
    Pair<long[], long[]> hashPair = new Pair<>(new long[Curl.HASH_LENGTH], new long[Curl.HASH_LENGTH]);
    Converter.trits(TRYTES, inTrits, 0);
    // Using reflection to benchmark private, non-production code.
    // Reflection doesn't have impact on benchmark result (this has been tested)
    // Please remove this code when method are public
    Class<Curl> curlClass = Curl.class;
    Constructor<Curl> curlConstructor = curlClass.getDeclaredConstructor(boolean.class, SpongeFactory.Mode.class);
    curlConstructor.setAccessible(true);
    Curl curl = curlConstructor.newInstance(true, SpongeFactory.Mode.CURLP81);
    Method pairAbsorb = curlClass.getDeclaredMethod("absorb", Pair.class, int.class, int.class);
    Method pairSqueeze = curlClass.getDeclaredMethod("squeeze", Pair.class, int.class, int.class);
    pairAbsorb.setAccessible(true);
    pairSqueeze.setAccessible(true);

    pairAbsorb.invoke(curl, Converter.longPair(inTrits), 0, inTrits.length);
    pairSqueeze.invoke(curl, hashPair, 0, Curl.HASH_LENGTH);
    byte[] hashTrits = Converter.trits(hashPair.low, hashPair.hi);
    String outTrytes = Converter.trytes(hashTrits);
    Assert.assertEquals(HASH, outTrytes);
  }

}
