package rs.ltt.push;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.firebase.messaging.Message;
import rs.ltt.jmap.annotation.Type;
import rs.ltt.jmap.common.entity.AbstractIdentifiableEntity;
import rs.ltt.jmap.common.entity.PushMessage;
import rs.ltt.jmap.common.entity.PushVerification;
import rs.ltt.jmap.common.entity.StateChange;
import rs.ltt.jmap.common.util.Mapper;

import java.util.List;
import java.util.Map;

public class Messages {

    private static final String DATA_KEY_TYPE = "type";
    private static final String DATA_KEY_CID = "cid";
    private static final String DATA_KEY_ACCOUNT = "account";

    public static List<Message> of(final String token, final long cid, final StateChange stateChange) {
        final ImmutableList.Builder<Message> listBuilder = new ImmutableList.Builder<>();
        final Map<String, Map<Class<? extends AbstractIdentifiableEntity>, String>> changed = stateChange.getChanged();
        for (Map.Entry<String, Map<Class<? extends AbstractIdentifiableEntity>, String>> accountChange : changed.entrySet()) {
            final String account = accountChange.getKey();
            final Map<Class<? extends AbstractIdentifiableEntity>, String> states = accountChange.getValue();
            final Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .putData(DATA_KEY_TYPE, getType(stateChange))
                    .putData(DATA_KEY_CID, String.valueOf(cid))
                    .putData(DATA_KEY_ACCOUNT, account);
            for (Map.Entry<Class<? extends AbstractIdentifiableEntity>, String> state : states.entrySet()) {
                final String entity = Mapper.ENTITIES.inverse().get(state.getKey());
                if (entity != null) {
                    messageBuilder.putData(entity, state.getValue());
                }
            }
            listBuilder.add(messageBuilder.build());
        }
        return listBuilder.build();
    }

    private static String getType(final PushMessage pushMessage) {
        final Class<? extends PushMessage> clazz = pushMessage.getClass();
        final Type annotation = clazz.getAnnotation(Type.class);
        if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
            return clazz.getSimpleName();
        } else {
            return annotation.value();
        }
    }

    public static Message of(final String token, final long cid, final PushVerification verification) {
        return Message.builder()
                .setToken(token)
                .putData(DATA_KEY_TYPE, getType(verification))
                .putData(DATA_KEY_CID, String.valueOf(cid))
                .putData("verificationCode", verification.getVerificationCode())
                .putData("subscriptionId", verification.getPushSubscriptionId())
                .build();
    }

}
