package org.zwobble.shed.compiler.types;

import java.util.Iterator;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.zwobble.shed.compiler.Option;
import org.zwobble.shed.compiler.typechecker.ValueInfo;
import org.zwobble.shed.compiler.util.ShedMaps;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

import static org.zwobble.shed.compiler.types.Member.member;

@ToString
@EqualsAndHashCode
public class Members implements Iterable<Member> {
    private final Map<String, Member> members;

    public static MembersBuilder builder() {
        return new MembersBuilder();
    }
    
    public static Members members() {
        return members(ImmutableMap.<String, ValueInfo>of());
    }
    
    public static Members members(String name1, ValueInfo value1) {
        return members(ImmutableMap.of(name1, value1));
    }
    
    public static Members members(String name1, ValueInfo value1, String name2, ValueInfo value2) {
        return members(ImmutableMap.of(name1, value1, name2, value2));
    }
    
    public static Members members(Map<String, ValueInfo> members) {
        return new Members(ImmutableMap.copyOf(Maps.transformEntries(members, toMember())));
    }
    
    public static Members members(Iterable<Member> members) {
        return new Members(ImmutableMap.copyOf(ShedMaps.toMapWithKeys(members, memberName())));
    }
    
    private static Function<Member, String> memberName() {
        return new Function<Member, String>() {
            @Override
            public String apply(Member input) {
                return input.getName();
            }
        };
    }

    private static EntryTransformer<String, ValueInfo, Member> toMember() {
        return new EntryTransformer<String, ValueInfo, Member>() {
            @Override
            public Member transformEntry(String key, ValueInfo value) {
                return member(key, value);
            }
        };
    }

    private Members(Map<String, Member> members) {
        this.members = members;
    }

    @Override
    public Iterator<Member> iterator() {
        return members.values().iterator();
    }
    
    public Option<Member> lookup(String name) {
        return members.containsKey(name) ? Option.some(members.get(name)) : Option.<Member>none();
    }
}
