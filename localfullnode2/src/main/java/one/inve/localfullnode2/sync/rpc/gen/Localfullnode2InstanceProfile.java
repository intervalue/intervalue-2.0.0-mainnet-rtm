// **********************************************************************
//
// Copyright (c) 2003-2018 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************
//
// Ice version 3.7.1
//
// <auto-generated>
//
// Generated from file `sync.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package one.inve.localfullnode2.sync.rpc.gen;

public class Localfullnode2InstanceProfile implements java.lang.Cloneable,
                                                      java.io.Serializable
{
    public int shardId;

    public int creatorId;

    public int nValue;

    public String dbId;

    public Localfullnode2InstanceProfile()
    {
        this.dbId = "";
    }

    public Localfullnode2InstanceProfile(int shardId, int creatorId, int nValue, String dbId)
    {
        this.shardId = shardId;
        this.creatorId = creatorId;
        this.nValue = nValue;
        this.dbId = dbId;
    }

    public boolean equals(java.lang.Object rhs)
    {
        if(this == rhs)
        {
            return true;
        }
        Localfullnode2InstanceProfile r = null;
        if(rhs instanceof Localfullnode2InstanceProfile)
        {
            r = (Localfullnode2InstanceProfile)rhs;
        }

        if(r != null)
        {
            if(this.shardId != r.shardId)
            {
                return false;
            }
            if(this.creatorId != r.creatorId)
            {
                return false;
            }
            if(this.nValue != r.nValue)
            {
                return false;
            }
            if(this.dbId != r.dbId)
            {
                if(this.dbId == null || r.dbId == null || !this.dbId.equals(r.dbId))
                {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    public int hashCode()
    {
        int h_ = 5381;
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, "::one::inve::localfullnode2::sync::rpc::gen::Localfullnode2InstanceProfile");
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, shardId);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, creatorId);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, nValue);
        h_ = com.zeroc.IceInternal.HashUtil.hashAdd(h_, dbId);
        return h_;
    }

    public Localfullnode2InstanceProfile clone()
    {
        Localfullnode2InstanceProfile c = null;
        try
        {
            c = (Localfullnode2InstanceProfile)super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return c;
    }

    public void ice_writeMembers(com.zeroc.Ice.OutputStream ostr)
    {
        ostr.writeInt(this.shardId);
        ostr.writeInt(this.creatorId);
        ostr.writeInt(this.nValue);
        ostr.writeString(this.dbId);
    }

    public void ice_readMembers(com.zeroc.Ice.InputStream istr)
    {
        this.shardId = istr.readInt();
        this.creatorId = istr.readInt();
        this.nValue = istr.readInt();
        this.dbId = istr.readString();
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, Localfullnode2InstanceProfile v)
    {
        if(v == null)
        {
            _nullMarshalValue.ice_writeMembers(ostr);
        }
        else
        {
            v.ice_writeMembers(ostr);
        }
    }

    static public Localfullnode2InstanceProfile ice_read(com.zeroc.Ice.InputStream istr)
    {
        Localfullnode2InstanceProfile v = new Localfullnode2InstanceProfile();
        v.ice_readMembers(istr);
        return v;
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, java.util.Optional<Localfullnode2InstanceProfile> v)
    {
        if(v != null && v.isPresent())
        {
            ice_write(ostr, tag, v.get());
        }
    }

    static public void ice_write(com.zeroc.Ice.OutputStream ostr, int tag, Localfullnode2InstanceProfile v)
    {
        if(ostr.writeOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            int pos = ostr.startSize();
            ice_write(ostr, v);
            ostr.endSize(pos);
        }
    }

    static public java.util.Optional<Localfullnode2InstanceProfile> ice_read(com.zeroc.Ice.InputStream istr, int tag)
    {
        if(istr.readOptional(tag, com.zeroc.Ice.OptionalFormat.FSize))
        {
            istr.skip(4);
            return java.util.Optional.of(Localfullnode2InstanceProfile.ice_read(istr));
        }
        else
        {
            return java.util.Optional.empty();
        }
    }

    private static final Localfullnode2InstanceProfile _nullMarshalValue = new Localfullnode2InstanceProfile();

    public static final long serialVersionUID = 2042084301L;
}
