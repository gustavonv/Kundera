/*******************************************************************************
 * * Copyright 2012 Impetus Infotech.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 ******************************************************************************/
package com.impetus.client.cassandra.schemamanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.ColumnDef;
import org.apache.cassandra.thrift.IndexType;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.cassandra.thrift.SchemaDisagreementException;
import org.apache.cassandra.thrift.TBinaryProtocol;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impetus.kundera.client.ClientType;
import com.impetus.kundera.configure.schema.ColumnInfo;
import com.impetus.kundera.configure.schema.SchemaGenerationException;
import com.impetus.kundera.configure.schema.TableInfo;
import com.impetus.kundera.configure.schema.api.AbstractSchemaManager;
import com.impetus.kundera.configure.schema.api.SchemaManager;

/**
 * Manages auto schema operation defined in {@code ScheamOperationType}.
 * 
 * @author Kuldeep.kumar
 * 
 */
public class CassandraSchemaManager extends AbstractSchemaManager implements SchemaManager
{
    /**
     * Cassandra client variable holds the client.
     */
    private Cassandra.Client cassandra_client;

    /**
     * logger used for logging statement.
     */
    private static final Logger logger = LoggerFactory.getLogger(CassandraSchemaManager.class);

    /**
     * Instantiates a new cassandra schema manager.
     *
     * @param client the client
     */
    public CassandraSchemaManager(ClientType client)
    {
        super(client);
    }

    @Override
    /**
     * Export schema handles the handleOperation method.
     */
    public void exportSchema()
    {
        super.exportSchema();
    }

    /**
     * create_drop method creates schema and table for the list of tableInfos.
     * 
     * @param tableInfos
     *            list of TableInfos.
     */
    protected void create_drop(List<TableInfo> tableInfos)
    {
        create(tableInfos);

    }

    /**
     * Creates schema and table for the list of tableInfos.
     * 
     * @param tableInfos
     *            list of TableInfos.
     */
    protected void create(List<TableInfo> tableInfos)
    {
        try
        {
            KsDef ksDef = cassandra_client.describe_keyspace(databaseName);
            addTablesToKeyspace(tableInfos, ksDef);
        }
        catch (NotFoundException nfex)
        {
            createKeyspaceAndTables(tableInfos);
        }
        catch (InvalidRequestException irex)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + irex.getMessage());
            throw new SchemaGenerationException(irex, "Cassandra", databaseName);
        }
        catch (TException tex)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + tex.getMessage());
            throw new SchemaGenerationException(tex, "Cassandra", databaseName);
        }
        catch (SchemaDisagreementException sdex)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + sdex.getMessage());
            throw new SchemaGenerationException(sdex, "Cassandra", databaseName);
        }
    }

    /**
     * update method update schema and table for the list of tableInfos
     * 
     * @param tableInfos
     *            list of TableInfos.
     */
    protected void update(List<TableInfo> tableInfos)
    {

        try
        {
            KsDef ksDef = cassandra_client.describe_keyspace(databaseName);
            addTablesToKeyspace(tableInfos, ksDef);
        }
        catch (NotFoundException e)
        {
            createKeyspaceAndTables(tableInfos);
        }
        catch (InvalidRequestException e)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
        catch (TException e)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
        catch (SchemaDisagreementException e)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
    }

    /**
     * validate method validate schema and table for the list of tableInfos.
     * 
     * @param tableInfos
     *            list of TableInfos.
     */
    protected void validate(List<TableInfo> tableInfos)
    {
        try
        {
            KsDef ksDef = cassandra_client.describe_keyspace(databaseName);
            onValidateTables(tableInfos, ksDef);
        }
        catch (NotFoundException e)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
        catch (InvalidRequestException e)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
        catch (TException e)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
    }

    /**
     * check for Tables method check the existence of schema and table
     * 
     * @param tableInfos
     *            list of TableInfos and ksDef object of KsDef
     */
    private void onValidateTables(List<TableInfo> tableInfos, KsDef ksDef)
    {
        try
        {
            cassandra_client.set_keyspace(ksDef.getName());
        }
        catch (InvalidRequestException e)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
        catch (TException e)
        {
            logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
        for (TableInfo tableInfo : tableInfos)
        {
            boolean found = false;
            for (CfDef cfDef : ksDef.getCf_defs())
            {
                if (cfDef.getName().equalsIgnoreCase(tableInfo.getTableName())
                        && (cfDef.getColumn_type().equals(tableInfo.getType())))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                logger.error("column family " + tableInfo.getTableName() + " does not exist in keyspace "
                        + databaseName + "");
                throw new SchemaGenerationException("Cassandra", databaseName, tableInfo.getTableName());
            }
        }
    }

    /**
     * add tables to Keyspace method add the table to given keyspace.
     * 
     * @param tableInfos
     *            list of TableInfos and ksDef object of KsDef.
     */
    private void addTablesToKeyspace(List<TableInfo> tableInfos, KsDef ksDef) throws InvalidRequestException,
            TException, SchemaDisagreementException
    {

        cassandra_client.set_keyspace(databaseName);
        for (TableInfo tableInfo : tableInfos)
        {
            boolean found = false;
            for (CfDef cfDef : ksDef.getCf_defs())
            {
                if (cfDef.getName().equalsIgnoreCase(tableInfo.getTableName()))
                {
                    found = true;
                    cassandra_client.system_drop_column_family(tableInfo.getTableName());
                    cassandra_client.system_add_column_family(getTableMetadata(tableInfo));
                    break;
                }
            }
            if (!found)
            {
                cassandra_client.system_add_column_family(getTableMetadata(tableInfo));
            }
        }
    }

    /**
     * create keyspace and table method create keyspace and table for the list
     * of tableInfos
     * 
     * @param tableInfos
     *            list of TableInfos.
     */
    private void createKeyspaceAndTables(List<TableInfo> tableInfos)
    {
        KsDef ksDef = new KsDef(databaseName, "org.apache.cassandra.locator.SimpleStrategy", null);
        ksDef.setReplication_factor(1);
        List<CfDef> cfDefs = new ArrayList<CfDef>();
        for (TableInfo tableInfo : tableInfos)
        {
            cfDefs.add(getTableMetadata(tableInfo));
        }
        ksDef.setCf_defs(cfDefs);
        try
        {
            createKeyspace(ksDef);
        }
        catch (InvalidRequestException e)
        {
            logger.error("Error during creating schema in cassandra, Caused by:" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
        catch (SchemaDisagreementException e)
        {
            logger.error("Error during creating schema in cassandra, Caused by:" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
        catch (TException e)
        {
            logger.error("Error during creating schema in cassandra, Caused by:" + e.getMessage());
            throw new SchemaGenerationException(e, "Cassandra", databaseName);
        }
    }

    /**
     * create keyspace method create keyspace for given ksDef.
     * 
     * @param ksDef
     *            a Object of KsDef.
     */
    private void createKeyspace(KsDef ksDef) throws InvalidRequestException, SchemaDisagreementException, TException
    {
        cassandra_client.system_add_keyspace(ksDef);
    }

    /**
     * get Table metadata method returns the metadata of table for given
     * tableInfo
     */
    /**
     * @param tableInfo
     * @return CfDef object
     */
    private CfDef getTableMetadata(TableInfo tableInfo)
    {
        CfDef cfDef = new CfDef();
        cfDef.setKeyspace(databaseName);
        cfDef.setName(tableInfo.getTableName());
        cfDef.setKey_validation_class(CassandraValidationClassMapper.getValidationClass(tableInfo.getTableIdType()));
        if (tableInfo.getType().equals("Standard"))
        {
            cfDef.setColumn_type(tableInfo.getType());
            List<ColumnDef> columnDefs = new ArrayList<ColumnDef>();
            List<ColumnInfo> columnInfos = tableInfo.getColumnMetadatas();
            for (ColumnInfo columnInfo : columnInfos)
            {
                ColumnDef columnDef = new ColumnDef();
                if (useSecondryIndex && columnInfo.isIndexable())
                {
                    columnDef.setIndex_type(IndexType.KEYS);
                }
                columnDef.setName(columnInfo.getColumnName().getBytes());
                columnDef.setValidation_class(CassandraValidationClassMapper.getValidationClass(columnInfo.getType()));
                columnDefs.add(columnDef);
            }
            cfDef.setColumn_metadata(columnDefs);
        }
        else
        {
            cfDef.setColumn_type("Super");
        }
        return cfDef;
    }

    /**
     * drop schema method drop the table from keyspace.
     */
    public void dropSchema()
    {
        if (operation != null && operation.equalsIgnoreCase("create-drop"))
        {
            try
            {
                cassandra_client.set_keyspace(databaseName);
                for (TableInfo tableInfo : tableInfos)
                {
                    cassandra_client.system_drop_column_family(tableInfo.getTableName());
                }
            }
            catch (InvalidRequestException e)
            {
                logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
                throw new SchemaGenerationException(e, "Cassandra");
            }
            catch (TException e)
            {
                logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
                throw new SchemaGenerationException(e, "Cassandra");
            }
            catch (SchemaDisagreementException e)
            {
                logger.error("keyspace " + databaseName + " does not exist :" + e.getMessage());
                throw new SchemaGenerationException(e, "Cassandra");
            }
        }
        cassandra_client = null;
    }

    /**
     * initiate client method initiates the client.
     * 
     * @return boolean value ie client started or not.
     * 
     */
    protected boolean initiateClient()
    {
        if (kundera_client.equalsIgnoreCase(ClientType.PELOPS.name()) && cassandra_client == null)
        {
            TSocket socket = new TSocket(host, Integer.parseInt(port));
            TTransport transport = new TFramedTransport(socket);
            TProtocol protocol = new TBinaryProtocol(transport);
            cassandra_client = new Cassandra.Client(protocol);
            try
            {
                if (!socket.isOpen())
                {
                    socket.open();
                }
            }
            catch (TTransportException e)
            {
                logger.error("Error while opening socket , Caused by:" + e.getMessage());
                throw new SchemaGenerationException(e, "Cassandra");
            }
            return true;
        }
        return false;
    }
}